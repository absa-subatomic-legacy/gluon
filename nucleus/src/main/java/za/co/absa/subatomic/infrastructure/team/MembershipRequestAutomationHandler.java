package za.co.absa.subatomic.infrastructure.team;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import za.co.absa.subatomic.domain.team.NewMembershipRequest;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MembershipRequestAutomationHandler {

    private TeamMemberRepository teamMemberRepository;

    private TeamRepository teamRepository;
    private RestTemplate restTemplate;

    public MembershipRequestAutomationHandler(TeamMemberRepository teamMemberRepository, TeamRepository teamRepository, RestTemplate restTemplate) {
        this.teamMemberRepository = teamMemberRepository;
        this.teamRepository = teamRepository;
        this.restTemplate = restTemplate;
    }

    @EventHandler
    public void on(NewMembershipRequest event) {
        log.info("Membership to a team has been requested, sending event to Atomist...");

        TeamMemberEntity requestedBy = teamMemberRepository.findByMemberId(event.getRequestedBy().getTeamMemberId());
        za.co.absa.subatomic.domain.member.SlackIdentity requestedBySlackIdentity = null;
        if (requestedBy.getSlackDetails() != null) {
            requestedBySlackIdentity = new za.co.absa.subatomic.domain.member.SlackIdentity(
                    requestedBy.getSlackDetails()
                            .getScreenName(),
                    requestedBy.getSlackDetails()
                            .getUserId());
        }

        TeamEntity teamEntity = teamRepository.findByTeamId(event.getTeamId());
        za.co.absa.subatomic.domain.team.SlackIdentity teamEntitySlackIdentity = null;
        if (teamEntity.getSlackDetails() != null) {
            teamEntitySlackIdentity = new za.co.absa.subatomic.domain.team.SlackIdentity(
                    teamEntity.getSlackDetails()
                            .getTeamChannel());
        }

        Team team = new Team(teamEntity.getTeamId(), teamEntity.getName(), teamEntitySlackIdentity);
        TeamMember member = new TeamMember(requestedBy.getMemberId(), requestedBy.getDomainUsername(), requestedBy.getFirstName(), requestedBy.getLastName(), requestedBy.getEmail(), requestedBySlackIdentity);

        NewMembershipRequestWithDetails newRequest = new NewMembershipRequestWithDetails(
                event,
                team,
                member
        );


        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://webhook.atomist.com/atomist/teams/T8RGCS6T0/ingestion/TeamCreatedEvent/cfd69c30-5ba0-453a-bf61-2314af439428",
                newRequest,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @Value
    private class NewMembershipRequestWithDetails {
        NewMembershipRequest newMembershipRequest;
        Team team;
        TeamMember requestedBy;
    }

    @Value
    private class Team {

        private String teamId;

        private String name;

        private za.co.absa.subatomic.domain.team.SlackIdentity slackIdentity;

        private final List<TeamMember> owners = new ArrayList<>();
    }

    @Value
    private class TeamMember {

        private String memberId;

        private String domainUsername;

        private String firstName;

        private String lastName;

        private String email;

        private za.co.absa.subatomic.domain.member.SlackIdentity slackIdentity;
    }
}
