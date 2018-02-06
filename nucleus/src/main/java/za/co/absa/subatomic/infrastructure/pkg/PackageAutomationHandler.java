package za.co.absa.subatomic.infrastructure.pkg;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import za.co.absa.subatomic.domain.member.SlackIdentity;
import za.co.absa.subatomic.domain.pkg.PackageCreated;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class PackageAutomationHandler {

    private RestTemplate restTemplate;

    private TeamMemberRepository teamMemberRepository;

    public PackageAutomationHandler(RestTemplate restTemplate,
            TeamMemberRepository teamMemberRepository) {
        this.restTemplate = restTemplate;
        this.teamMemberRepository = teamMemberRepository;
    }

    @EventHandler
    void on(PackageCreated event) {
        log.info("A package was created, sending event to Atomist: {}",
                event);

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

        PackageCreatedWithDetails newProject = new PackageCreatedWithDetails(
                event,
                new CreatedBy(
                        teamMemberEntity.getMemberId(),
                        teamMemberEntity.getFirstName(),
                        teamMemberEntity.getLastName(),
                        teamMemberEntity.getEmail(),
                        slackIdentity));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://webhook.atomist.com/atomist/teams/T8RGCS6T0/ingestion/ProjectCreatedEvent/6941caa4-c0fd-487c-baf6-292014c89e45",
                newProject,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @Value
    class PackageCreatedWithDetails {

        private PackageCreated pkg;

        private CreatedBy createdBy;
    }

    @Value
    class CreatedBy {

        private String memberId;

        private String firstName;

        private String lastName;

        private String email;

        private SlackIdentity slackIdentity;
    }
}
