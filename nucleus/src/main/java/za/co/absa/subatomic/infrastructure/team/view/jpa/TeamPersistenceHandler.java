package za.co.absa.subatomic.infrastructure.team.view.jpa;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.adapter.metadata.rest.MetadataEntryResource;
import za.co.absa.subatomic.adapter.metadata.rest.MetadataResource;
import za.co.absa.subatomic.domain.exception.InvalidRequestException;
import za.co.absa.subatomic.domain.team.MembershipRequestStatus;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;
import za.co.absa.subatomic.infrastructure.metadata.MetadataEntity;
import za.co.absa.subatomic.infrastructure.metadata.MetadataEntry;

import static java.util.stream.Collectors.toList;

@Component
public class TeamPersistenceHandler {

    private TeamRepository teamRepository;

    private TeamMemberRepository teamMemberRepository;

    private MembershipRequestRepository membershipRequestRepository;

    public TeamPersistenceHandler(TeamRepository teamRepository,
                                  TeamMemberRepository teamMemberRepository,
                                  MembershipRequestRepository membershipRequestRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.membershipRequestRepository = membershipRequestRepository;
    }

    @Transactional
    public TeamEntity createTeam(String name, String description,
                                 String openShiftCloud,
                                 String slackTeamChannel,
                                 String createdById,
                                 List<MetadataResource> metadataResources) {

        TeamMemberEntity createdBy = this.teamMemberRepository
                .findByMemberId(createdById);

        List<MetadataEntity> metadataEntities;

        metadataEntities = metadataResources
                .stream()
                .map(temp -> {
                    List<MetadataEntry> metadataEntries = temp.getMetadataEntries()
                            .stream()
                            .map(temp2 -> MetadataEntry.builder().key(temp2.getKey()).value(temp2.getValue()).build())
                            .collect(toList());
                    return MetadataEntity.builder().metadataEntries(metadataEntries).description(temp.getDescription()).build();
                })
                .collect(Collectors.toList());

        TeamEntity teamEntity = TeamEntity.builder()
                .teamId(UUID.randomUUID().toString())
                .description(description)
                .name(name)
                .openShiftCloud(openShiftCloud)
                .createdBy(createdBy)
                .owners(Collections.singleton(createdBy))
                .metadata(metadataEntities)
                .build();

        if (slackTeamChannel != null) {
            teamEntity.setSlackDetails(
                    new SlackDetailsEmbedded(slackTeamChannel));
        }

        teamEntity = this.teamRepository.save(teamEntity);

        createdBy.getTeams().add(teamEntity);

        this.teamMemberRepository.save(createdBy);

        return teamEntity;
    }

    @Transactional
    public TeamEntity addSlackIdentity(String teamId, String teamChannel) {
        TeamEntity team = teamRepository.findByTeamId(teamId);
        if (team == null) {
            throw new InvalidRequestException(MessageFormat.format(
                    "Could not find team with ID {}.", teamId));
        }
        team.setSlackDetails(new SlackDetailsEmbedded(teamChannel));

        return teamRepository.save(team);
    }

    @Transactional
    public void addTeamMembers(String teamId,
                               List<String> teamOwnerIdsRequested,
                               List<String> teamMemberIdsRequested) {
        TeamEntity team = teamRepository.findByTeamId(teamId);

        List<String> existingMemberIds = team.getMembers().stream()
                .map(TeamMemberEntity::getMemberId)
                .collect(Collectors.toList());

        List<String> existingOwnerIds = team.getOwners().stream()
                .map(TeamMemberEntity::getMemberId)
                .collect(Collectors.toList());

        // Filter existing members
        List<String> teamMemberIds = teamMemberIdsRequested.stream()
                .filter(memberId -> !existingMemberIds.contains(memberId))
                .collect(Collectors.toList());

        // Filter existing owners
        List<String> teamOwnerIds = teamOwnerIdsRequested.stream()
                .filter(memberId -> !existingOwnerIds.contains(memberId))
                .collect(Collectors.toList());

        for (String ownerId : teamOwnerIds) {
            Optional<TeamMemberEntity> newOwnerWasMemberResult = team
                    .getMembers().stream()
                    .filter(memberEntity -> memberEntity.getMemberId()
                            .equals(ownerId))
                    .findFirst();

            newOwnerWasMemberResult.ifPresent(
                    memberEntity -> team.getMembers().remove(memberEntity));

            TeamMemberEntity newOwner = newOwnerWasMemberResult
                    .orElse(this.teamMemberRepository.findByMemberId(ownerId));

            team.getOwners().add(newOwner);
            newOwner.getTeams().add(team);
            teamMemberRepository.save(newOwner);
        }

        for (String memberId : teamMemberIds) {
            Optional<TeamMemberEntity> newMemberWasOwner = team
                    .getOwners().stream()
                    .filter(memberEntity -> memberEntity.getMemberId()
                            .equals(memberId))
                    .findFirst();

            newMemberWasOwner.ifPresent(
                    memberEntity -> team.getOwners().remove(memberEntity));

            TeamMemberEntity newMember = newMemberWasOwner
                    .orElse(this.teamMemberRepository.findByMemberId(memberId));

            team.getMembers().add(newMember);
            newMember.getTeams().add(team);
            teamMemberRepository.save(newMember);
        }

        teamRepository.save(team);
    }

    @Transactional
    public void removeTeamMembers(String teamId,
                                  List<String> teamOwnerIdsRequested,
                                  List<String> teamMemberIdsRequested) {
        TeamEntity team = teamRepository.findByTeamId(teamId);

        team.setMembers(team.getMembers().stream()
                .filter(teamMemberEntity -> teamMemberIdsRequested
                        .contains(teamMemberEntity.getMemberId()))
                .collect(Collectors.toSet()));

        team.setOwners(team.getOwners().stream()
                .filter(teamMemberEntity -> teamOwnerIdsRequested
                        .contains(teamMemberEntity.getMemberId()))
                .collect(Collectors.toSet()));

        teamOwnerIdsRequested.forEach(teamMemberId -> {
            TeamMemberEntity memberEntity = teamMemberRepository
                    .findByMemberId(teamMemberId);
            memberEntity.getTeams().remove(team);
            teamMemberRepository.save(memberEntity);
        });

        teamMemberIdsRequested.forEach(teamMemberId -> {
            TeamMemberEntity memberEntity = teamMemberRepository
                    .findByMemberId(teamMemberId);
            memberEntity.getTeams().remove(team);
            teamMemberRepository.save(memberEntity);
        });

        teamRepository.save(team);
    }

    @Transactional
    public void removeTeamMember(String teamId,
                                 String memberId) {

        TeamEntity team = teamRepository.findByTeamId(teamId);

        TeamMemberEntity member = this.teamMemberRepository
                .findByMemberId(memberId);

        member.getTeams().remove(team);
        teamMemberRepository.save(member);

        team.getMembers().remove(member);
        teamRepository.save(team);
    }

    @Transactional
    public MembershipRequestEntity closeMembershipRequest(
            String membershipRequestId, MembershipRequestStatus status,
            String closedById) {
        TeamMemberEntity closedBy = this.teamMemberRepository
                .findByMemberId(closedById);
        MembershipRequestEntity membershipRequest = this.membershipRequestRepository
                .findByMembershipRequestId(membershipRequestId);

        membershipRequest.setRequestStatus(status);

        membershipRequest.setApprovedBy(closedBy);

        return this.membershipRequestRepository
                .save(membershipRequest);
    }

    @Transactional
    public MembershipRequestEntity createMembershipRequest(String teamId,
                                                           String requestedById) {
        TeamEntity team = teamRepository.findByTeamId(teamId);
        TeamMemberEntity requestedBy = this.teamMemberRepository
                .findByMemberId(requestedById);

        MembershipRequestEntity membershipRequestEntity = MembershipRequestEntity
                .builder()
                .membershipRequestId(UUID.randomUUID().toString())
                .teamId(teamId)
                .requestedBy(requestedBy)
                .requestStatus(MembershipRequestStatus.OPEN)
                .build();

        membershipRequestEntity = this.membershipRequestRepository
                .save(membershipRequestEntity);

        team.getMembershipRequests().add(membershipRequestEntity);
        teamRepository.save(team);
        return membershipRequestEntity;
    }

    @Transactional
    public TeamEntity updateOpenShiftCloud(TeamEntity team,
                                           String openShiftCloud) {
        team.setOpenShiftCloud(openShiftCloud);
        return this.teamRepository.save(team);
    }

    @Transactional
    public TeamEntity updateTeamMetadata(TeamEntity team, List<MetadataResource> metadata, boolean overwrite) {

        if (overwrite == true) {

            List<MetadataEntity> metadataEntities;

            metadataEntities = metadata
                    .stream()
                    .map(temp -> {
                        List<MetadataEntry> metadataEntries = temp.getMetadataEntries()
                                .stream()
                                .map(temp2 -> MetadataEntry.builder().key(temp2.getKey()).value(temp2.getValue()).build())
                                .collect(toList());
                        return MetadataEntity.builder().metadataEntries(metadataEntries).description(temp.getDescription()).build();
                    })
                    .collect(Collectors.toList());

            team.setMetadata(metadataEntities);

            return this.teamRepository.save(team);
        } else {

            for (MetadataResource metadataResource : metadata) {
                List<MetadataEntity> teamMetadata = team.getMetadata();
                Optional<MetadataEntity> firstMatchingMetadataEntity = teamMetadata.stream().filter(metadataEntity -> metadataEntity.getDescription().equals(metadataResource.getDescription())).findFirst();
                if (firstMatchingMetadataEntity.isPresent()) {
                    MetadataEntity matchedMetadataEntity = firstMatchingMetadataEntity.get();
                    for (MetadataEntryResource metadataEntryResource : metadataResource.getMetadataEntries()) {
                        Optional<MetadataEntry> firstMatchedEntry = matchedMetadataEntity.getMetadataEntries().stream().filter(metadataEntry -> metadataEntry.getKey().equals(metadataEntryResource.getKey())).findFirst();
                        if (firstMatchedEntry.isPresent()) {
                            // update the value
                            firstMatchedEntry.get().setValue(metadataEntryResource.getValue());
                        } else {
                            // add the key and value
                            matchedMetadataEntity.getMetadataEntries().add(MetadataEntry.builder().key(metadataEntryResource.getKey()).value(metadataEntryResource.getValue()).build());
                        }
                    }
                } else {
                    // Override entire object

                    List<MetadataEntry> metadataEntries = metadataResource.getMetadataEntries()
                            .stream()
                            .map(temp2 -> MetadataEntry.builder().key(temp2.getKey()).value(temp2.getValue()).build())
                            .collect(toList());
                    MetadataEntity metadataEntity = MetadataEntity.builder().metadataEntries(metadataEntries).description(metadataResource.getDescription()).build();
                    team.getMetadata().add(metadataEntity);
                }
            }
        }
        return this.teamRepository.save(team);
    }

    @Transactional(readOnly = true)
    public TeamEntity findByTeamId(String teamId) {
        return teamRepository.findByTeamId(teamId);
    }

    @Transactional(readOnly = true)
    public List<TeamEntity> findAll() {
        return teamRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TeamEntity findByName(String name) {
        return teamRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public MembershipRequestEntity findMembershipRequestById(String id) {
        return membershipRequestRepository.findByMembershipRequestId(id);
    }

    @Transactional(readOnly = true)
    public Set<TeamEntity> findByMemberOrOwnerMemberId(String teamMemberId) {
        Set<TeamEntity> teamsWithMemberOrOwner = new HashSet<>();
        teamsWithMemberOrOwner.addAll(teamRepository
                .findByMembers_MemberId(teamMemberId));
        teamsWithMemberOrOwner.addAll(teamRepository
                .findByOwners_MemberId(teamMemberId));
        return teamsWithMemberOrOwner;
    }

    @Transactional(readOnly = true)
    public Set<TeamEntity> findByMemberOrOwnerSlackScreenName(
            String slackScreenName) {
        Set<TeamEntity> teamsWithMemberOrOwner = new HashSet<>();
        teamsWithMemberOrOwner.addAll(teamRepository
                .findByMembers_SlackDetailsScreenName(slackScreenName));
        teamsWithMemberOrOwner.addAll(teamRepository
                .findByOwners_SlackDetailsScreenName(slackScreenName));
        return teamsWithMemberOrOwner;
    }

    @Transactional(readOnly = true)
    public List<TeamEntity> findBySlackTeamChannel(String slackTeamChannel) {
        return teamRepository.findBySlackDetailsTeamChannel(slackTeamChannel);
    }
}
