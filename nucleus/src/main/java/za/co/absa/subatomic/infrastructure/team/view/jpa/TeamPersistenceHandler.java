package za.co.absa.subatomic.infrastructure.team.view.jpa;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.exception.InvalidRequestException;
import za.co.absa.subatomic.domain.team.MembershipRequestStatus;
import za.co.absa.subatomic.domain.team.TeamCreated;
import za.co.absa.subatomic.domain.team.TeamDeleted;
import za.co.absa.subatomic.domain.team.TeamMembersAdded;
import za.co.absa.subatomic.domain.team.TeamMembersRemoved;
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

    @EventHandler
    @Transactional
    void on(TeamCreated event) {
        TeamMemberEntity createdBy = teamMemberRepository
                .findByMemberId(event.getCreatedBy().getTeamMemberId());

        TeamEntity teamEntity = TeamEntity.builder()
                .teamId(event.getTeamId())
                .name(event.getName())
                .description(event.getDescription())
                .createdBy(createdBy)
                .owners(Collections.singleton(createdBy))
                .build();
        createdBy.getTeams().add(teamEntity);

        event.getSlackIdentity()
                .ifPresent(slackIdentity -> teamEntity.setSlackDetails(
                        new SlackDetailsEmbedded(
                                slackIdentity.getTeamChannel())));

        teamRepository.save(teamEntity);
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

    @EventHandler
    @Transactional
    void on(TeamMembersAdded event) {
        TeamEntity team = teamRepository.findByTeamId(event.getTeamId());
        team.getOwners().addAll(event.getOwners().stream()
                .map(teamMemberId -> teamMemberRepository
                        .findByMemberId(teamMemberId.getTeamMemberId()))
                .collect(Collectors.toList()));

        team.getMembers().addAll(event.getTeamMembers().stream()
                .map(teamMemberId -> teamMemberRepository
                        .findByMemberId(teamMemberId.getTeamMemberId()))
                .collect(Collectors.toList()));

        event.getOwners()
                .forEach(teamMemberId -> teamMemberRepository
                        .findByMemberId(teamMemberId.getTeamMemberId())
                        .getTeams().add(team));

        event.getTeamMembers()
                .forEach(teamMemberId -> teamMemberRepository
                        .findByMemberId(teamMemberId.getTeamMemberId())
                        .getTeams().add(team));

        teamRepository.save(team);
    }

    @EventHandler
    @Transactional
    void on(TeamMembersRemoved event) {
        TeamEntity team = teamRepository.findByTeamId(event.getTeamId());
        team.getOwners().removeAll(event.getOwners().stream()
                .map(teamMemberId -> teamMemberRepository
                        .findByMemberId(teamMemberId.getTeamMemberId()))
                .collect(Collectors.toList()));

        team.getMembers().removeAll(event.getTeamMembers().stream()
                .map(teamMemberId -> teamMemberRepository
                        .findByMemberId(teamMemberId.getTeamMemberId()))
                .collect(Collectors.toList()));

        event.getOwners()
                .forEach(teamMemberId -> teamMemberRepository
                        .findByMemberId(teamMemberId.getTeamMemberId())
                        .getTeams().remove(team));

        event.getTeamMembers()
                .forEach(teamMemberId -> teamMemberRepository
                        .findByMemberId(teamMemberId.getTeamMemberId())
                        .getTeams().remove(team));

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

    @EventHandler
    @Transactional
    void on(TeamDeleted event) {
        TeamEntity teamEntity = teamRepository.findByTeamId(event.getTeamId());
        teamEntity.getOwners().forEach(teamMemberEntity -> {
            teamMemberEntity.getTeams().remove(teamEntity);
            teamMemberRepository.save(teamMemberEntity);
        });
        teamEntity.getMembers().forEach(teamMemberEntity -> {
            teamMemberEntity.getTeams().remove(teamEntity);
            teamMemberRepository.save(teamMemberEntity);
        });
        teamRepository.deleteByTeamId(event.getTeamId());
    }

}
