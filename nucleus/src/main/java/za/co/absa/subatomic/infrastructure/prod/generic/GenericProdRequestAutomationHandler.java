package za.co.absa.subatomic.infrastructure.prod.generic;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
import za.co.absa.subatomic.infrastructure.prod.generic.view.jpa.GenericProdRequestEntity;

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
        GenericProdRequest requestEvent = new GenericProdRequest(
                genericProdRequestEntity.getGenericProdRequestId());

        Gson gson = new Gson();

        String jsonRepresentation = gson.toJson(requestEvent);
        log.info("Sending payload to atomist: {}", jsonRepresentation);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getGenericProdRequestedEventUrl(),
                requestEvent,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info(
                    "Atomist has ingested ProjectProdEnvironmentsRequested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @Value
    private class GenericProdRequest {
        private String genericProdRequestId;
    }

}
