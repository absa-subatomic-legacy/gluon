package za.co.absa.subatomic.infrastructure.team.view.jpa;

import java.util.Collections;
import java.util.stream.Collectors;

import org.axonframework.eventhandling.EventHandler;
import za.co.absa.subatomic.domain.team.SlackIdentityAdded;
import za.co.absa.subatomic.domain.team.TeamCreated;
import za.co.absa.subatomic.domain.team.TeamMembersAdded;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TeamHandler {

    private TeamRepository teamRepository;

    private TeamMemberRepository teamMemberRepository;

    public TeamHandler(TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
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

    @EventHandler
    @Transactional
    void on(SlackIdentityAdded event) {
        TeamEntity team = teamRepository.findByTeamId(event.getTeamId());
        team.setSlackDetails(new SlackDetailsEmbedded(
                event.getSlackIdentity().getTeamChannel()));

        teamRepository.save(team);
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
}
