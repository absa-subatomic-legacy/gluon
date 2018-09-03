package za.co.absa.subatomic.infrastructure.prod.project;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
import za.co.absa.subatomic.infrastructure.prod.project.view.jpa.ProjectProdRequestEntity;

@Component
@Slf4j
public class ProjectProdRequestAutomationHandler {

    private RestTemplate restTemplate;

    private AtomistConfigurationProperties atomistConfigurationProperties;

    public ProjectProdRequestAutomationHandler(RestTemplate restTemplate,
            AtomistConfigurationProperties atomistConfigurationProperties) {
        this.restTemplate = restTemplate;
        this.atomistConfigurationProperties = atomistConfigurationProperties;
    }

    public void projectProdRequestCreated(
            ProjectProdRequestEntity projectProdRequestEntity) {

        ProjectProdRequest requestEvent = new ProjectProdRequest(
                projectProdRequestEntity.getProjectProdRequestId());

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getProjectProductionEnvironmentsRequestedEventUrl(),
                requestEvent,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @Value
    private class ProjectProdRequest {
        private String projectProdRequestId;
    }
}
