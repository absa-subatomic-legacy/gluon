package za.co.absa.subatomic.infrastructure.team;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import za.co.absa.subatomic.domain.member.SlackIdentity;
import za.co.absa.subatomic.domain.team.DevOpsEnvironmentRequested;
import za.co.absa.subatomic.domain.team.TeamCreated;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class TeamAutomationHandler {

    private TeamMemberRepository teamMemberRepository;

    private TeamRepository teamRepository;

    private RestTemplate restTemplate;

    public TeamAutomationHandler(TeamMemberRepository teamMemberRepository,
            TeamRepository teamRepository, RestTemplate restTemplate) {
        this.teamMemberRepository = teamMemberRepository;
        this.teamRepository = teamRepository;
        this.restTemplate = restTemplate;
    }

    @EventHandler
    public void on(TeamCreated event) {
        log.info("A team was created, sending event to Atomist...");

        TeamMemberEntity teamMemberEntity = teamMemberRepository
                .findByMemberId(event.getCreatedBy().getTeamMemberId());

        SlackIdentity slackIdentity = null;
        if (teamMemberEntity.getSlackDetails() != null) {
            slackIdentity = new SlackIdentity(
                    teamMemberEntity.getSlackDetails()
                            .getScreenName(),
                    teamMemberEntity.getSlackDetails()
                            .getUserId());
        }

        TeamCreatedWithDetails newTeam = new TeamCreatedWithDetails(event,
                new TeamMember(
                        teamMemberEntity.getMemberId(),
                        teamMemberEntity.getDomainUsername(),
                        teamMemberEntity.getFirstName(),
                        teamMemberEntity.getLastName(),
                        teamMemberEntity.getEmail(),
                        slackIdentity));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://webhook.atomist.com/atomist/teams/T8RGCS6T0/ingestion/TeamCreatedEvent/cfd69c30-5ba0-453a-bf61-2314af439428",
                newTeam,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @EventHandler
    public void on(DevOpsEnvironmentRequested event) {
        log.info(
                "A team DevOps environment was requested, sending event to Atomist...");

        TeamEntity teamEntity = teamRepository.findByTeamId(event.getTeamId());
        TeamMemberEntity teamMemberEntity = teamMemberRepository
                .findByMemberId(event.getRequestedBy().getTeamMemberId());

        DevOpsEnvironmentRequestedWithDetails newDevOpsEnvironmentRequested = new DevOpsEnvironmentRequestedWithDetails(
                new Team(
                        teamEntity.getTeamId(),
                        teamEntity.getName(),
                        new za.co.absa.subatomic.domain.team.SlackIdentity(
                                teamEntity.getSlackDetails().getTeamChannel())),
                new DevOpsTeamMember(
                        null,
                        teamMemberEntity.getFirstName(),
                        new SlackIdentity(
                                teamMemberEntity.getSlackDetails()
                                        .getScreenName(),
                                teamMemberEntity.getSlackDetails()
                                        .getUserId())));
        newDevOpsEnvironmentRequested.getTeam().getOwners().addAll(
                teamEntity.getOwners().stream()
                        .map(memberEntity -> new DevOpsTeamMember(
                                memberEntity.getDomainUsername(),
                                memberEntity.getFirstName(),
                                new SlackIdentity(
                                        memberEntity.getSlackDetails()
                                                .getScreenName(),
                                        memberEntity.getSlackDetails()
                                                .getUserId())))
                        .collect(Collectors.toList()));
        newDevOpsEnvironmentRequested.getTeam().getMembers().addAll(
                teamEntity.getMembers().stream()
                        .map(memberEntity -> new DevOpsTeamMember(
                                memberEntity.getDomainUsername(),
                                memberEntity.getFirstName(),
                                new SlackIdentity(
                                        memberEntity.getSlackDetails()
                                                .getScreenName(),
                                        memberEntity.getSlackDetails()
                                                .getUserId())))
                        .collect(Collectors.toList()));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://webhook.atomist.com/atomist/teams/T8RGCS6T0/ingestion/DevOpsEnvironmentRequestedEvent/2254e03f-abaa-4ce2-9fd4-482a59fda821",
                newDevOpsEnvironmentRequested,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @Value
    private class TeamCreatedWithDetails {

        private TeamCreated team;

        private TeamMember createdBy;
    }

    @Value
    private class DevOpsEnvironmentRequestedWithDetails {

        private Team team;

        private DevOpsTeamMember requestedBy;
    }

    @Value
    private class Team {

        private String teamId;

        private String name;

        private za.co.absa.subatomic.domain.team.SlackIdentity slackIdentity;

        private final List<DevOpsTeamMember> owners = new ArrayList<>();

        private final List<DevOpsTeamMember> members = new ArrayList<>();
    }

    @Value
    private class TeamMember {

        private String memberId;

        private String domainUsername;

        private String firstName;

        private String lastName;

        private String email;

        private SlackIdentity slackIdentity;
    }

    @Value
    private class DevOpsTeamMember {

        private String domainUsername;

        private String firstName;

        private SlackIdentity slackIdentity;
    }
}
