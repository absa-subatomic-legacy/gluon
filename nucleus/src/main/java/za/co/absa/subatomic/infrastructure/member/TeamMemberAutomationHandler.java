package za.co.absa.subatomic.infrastructure.member;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import za.co.absa.subatomic.domain.member.TeamMemberCreated;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class TeamMemberAutomationHandler {

    private RestTemplate restTemplate;

    public TeamMemberAutomationHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @EventHandler
    void on(TeamMemberCreated event) {
        log.info("A team member was created, sending event to Atomist: {}",
                event);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://webhook.atomist.com/atomist/teams/T8RGCS6T0/ingestion/TeamMemberCreatedEvent/f9089184-8399-4faf-bcb8-9c39b921ee14",
                event,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }
}
