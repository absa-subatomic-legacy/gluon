package za.co.absa.subatomic.infrastructure.prod.project;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;

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

}
