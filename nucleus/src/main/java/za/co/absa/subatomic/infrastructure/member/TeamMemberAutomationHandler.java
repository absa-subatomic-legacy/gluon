package za.co.absa.subatomic.infrastructure.member;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.domain.member.DomainCredentials;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
import za.co.absa.subatomic.infrastructure.atomist.resource.AtomistMemberCreated;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;

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
    public void teamMemberCreated(TeamMemberEntity memberEntity) {
        log.info("A team member was created, sending event to Atomist: {}",
                memberEntity);

        AtomistMemberCreated atomistTeamMember = AtomistMemberCreated
                .builder()
                .memberId(memberEntity.getMemberId())
                .domainCredentials(
                        new DomainCredentials(memberEntity.getDomainUsername()))
                .email(memberEntity.getEmail())
                .firstName(memberEntity.getFirstName())
                .lastName(memberEntity.getLastName())
                .slackIdentity(memberEntity.getSlackDetails())
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties.getTeamMemberCreatedEventUrl(),
                atomistTeamMember,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }
}
