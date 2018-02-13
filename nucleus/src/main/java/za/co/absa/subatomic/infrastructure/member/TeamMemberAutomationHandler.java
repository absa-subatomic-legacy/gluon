package za.co.absa.subatomic.infrastructure.member;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import za.co.absa.subatomic.domain.member.TeamMemberCreated;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import za.co.absa.subatomic.infrastructure.AtomistConfiguration;

@Component
@Slf4j
public class TeamMemberAutomationHandler {

    private RestTemplate restTemplate;
    private AtomistConfiguration atomistConfiguration;

    public TeamMemberAutomationHandler(RestTemplate restTemplate, AtomistConfiguration atomistConfiguration) {
        this.restTemplate = restTemplate;
        this.atomistConfiguration = atomistConfiguration;
    }

    @EventHandler
    void on(TeamMemberCreated event) {
        log.info("A team member was created, sending event to Atomist: {}",
                event);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfiguration.getTeamCreatedEventUrl(),
                event,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }
}
