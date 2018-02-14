package za.co.absa.subatomic.infrastructure.member;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import za.co.absa.subatomic.domain.member.TeamMemberCreated;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class TeamMemberAutomationHandler {

    private RestTemplate restTemplate;

    private AtomistConfigurationProperties atomistConfigurationProperties;

    public TeamMemberAutomationHandler(RestTemplate restTemplate,
            AtomistConfigurationProperties atomistConfigurationProperties) {
        this.restTemplate = restTemplate;
        this.atomistConfigurationProperties = atomistConfigurationProperties;
    }

    @EventHandler
    void on(TeamMemberCreated event) {
        log.info("A team member was created, sending event to Atomist: {}",
                event);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties.getTeamCreatedEventUrl(),
                event,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }
}
