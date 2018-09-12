package za.co.absa.subatomic.infrastructure.team.view.jpa;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.exception.InvalidRequestException;
import za.co.absa.subatomic.domain.team.MembershipRequestStatus;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;

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
            String slackTeamChannel,
            String createdById) {

        TeamMemberEntity createdBy = this.teamMemberRepository
                .findByMemberId(createdById);

        TeamEntity teamEntity = TeamEntity.builder()
                .teamId(UUID.randomUUID().toString())
                .description(description)
                .name(name)
                .createdBy(createdBy)
                .owners(Collections.singleton(createdBy))
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

        // Filter existing members
        List<String> teamMemberIds = teamMemberIdsRequested.stream()
                .filter(memberId -> !teamMemberIdsRequested.contains(memberId))
                .collect(Collectors.toList());

        // Filter existing owners
        List<String> teamOwnerIds = teamOwnerIdsRequested.stream()
                .filter(memberId -> !teamOwnerIdsRequested.contains(memberId))
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

}
