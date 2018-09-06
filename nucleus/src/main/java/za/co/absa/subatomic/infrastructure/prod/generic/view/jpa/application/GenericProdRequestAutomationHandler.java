package za.co.absa.subatomic.infrastructure.prod.generic.view.jpa.application;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
import za.co.absa.subatomic.infrastructure.prod.generic.view.jpa.application.view.jpa.GenericProdRequestEntity;

@Component
@Slf4j
public class GenericProdRequestAutomationHandler {

    private RestTemplate restTemplate;

    private AtomistConfigurationProperties atomistConfigurationProperties;

    public GenericProdRequestAutomationHandler(RestTemplate restTemplate,
            AtomistConfigurationProperties atomistConfigurationProperties) {
        this.restTemplate = restTemplate;
        this.atomistConfigurationProperties = atomistConfigurationProperties;
    }

    public void genericProdRequestCreated(
            GenericProdRequestEntity genericProdRequestEntity) {

    }

}
