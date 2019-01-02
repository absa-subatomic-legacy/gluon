package za.co.absa.subatomic.infrastructure.prod.application;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.domain.application.ApplicationCreated;
import za.co.absa.subatomic.domain.member.TeamMemberSlackIdentity;
import za.co.absa.subatomic.domain.team.TeamSlackIdentity;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationEntity;
import za.co.absa.subatomic.infrastructure.atomist.resource.AtomistTeamBase;
import za.co.absa.subatomic.infrastructure.atomist.resource.project.AtomistDeploymentPipeline;
import za.co.absa.subatomic.infrastructure.atomist.resource.project.AtomistProjectBase;
import za.co.absa.subatomic.infrastructure.atomist.resource.project.AtomistProjectMapper;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.prod.application.view.jpa.ApplicationProdRequestEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@Component
@Slf4j
public class ApplicationProdRequestAutomationHandler {

    private RestTemplate restTemplate;

    private AtomistConfigurationProperties atomistConfigurationProperties;

    public ApplicationProdRequestAutomationHandler(RestTemplate restTemplate,
            AtomistConfigurationProperties atomistConfigurationProperties) {
        this.restTemplate = restTemplate;
        this.atomistConfigurationProperties = atomistConfigurationProperties;
    }

    public void applicationProdRequestCreated(
            ApplicationProdRequestEntity applicationProdRequestEntity) {

        ApplicationProdRequest applicationProdRequest = new ApplicationProdRequest(
                applicationProdRequestEntity.getApplicationProdRequestId(),
                applicationProdRequestEntity.getCreatedAt());

        ApplicationEntity applicationEntity = applicationProdRequestEntity
                .getApplication();

        ApplicationCreated applicationCreated = this
                .applicationEntityToApplication(applicationEntity);

        ProjectEntity projectEntity = applicationEntity.getProject();

        AtomistProjectBase projectCreated = new AtomistProjectMapper()
                .createAtomistProjectBase(projectEntity);

        AtomistDeploymentPipeline deploymentPipeline = new AtomistProjectMapper()
                .createAtomistDeploymentPipeline(
                        applicationProdRequestEntity.getDeploymentPipeline());

        TeamEntity owningTeamEntity = projectEntity.getOwningTeam();

        AtomistTeamBase owningTeam = this.teamEntityToTeam(owningTeamEntity);

        List<AtomistTeamBase> associatedTeams = new ArrayList<>();
        for (TeamEntity team : projectEntity.getTeams()) {
            associatedTeams.add(this.teamEntityToTeam(team));
        }

        TeamMemberEntity actionedByEntity = applicationProdRequestEntity
                .getActionedBy();

        ActionedBy actionedBy = this.teamMemberToActionedBy(actionedByEntity);

        ApplicationProdRequestCreatedWithDetails applicationProdRequestEvent = new ApplicationProdRequestCreatedWithDetails(
                applicationProdRequest,
                applicationCreated,
                projectCreated,
                deploymentPipeline,
                owningTeam,
                associatedTeams,
                actionedBy);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getApplicationProdRequestedEventUrl(),
                applicationProdRequestEvent,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    private ApplicationCreated applicationEntityToApplication(
            ApplicationEntity applicationEntity) {
        return ApplicationCreated.builder()
                .applicationId(applicationEntity.getApplicationId())
                .applicationType(applicationEntity.getApplicationType())
                .description(applicationEntity.getDescription())
                .name(applicationEntity.getDescription())
                .build();
    }

    private AtomistTeamBase teamEntityToTeam(TeamEntity teamEntity) {
        TeamSlackIdentity teamSlackIdentity = null;
        if (teamEntity.getSlackDetails() != null) {
            teamSlackIdentity = new TeamSlackIdentity(
                    teamEntity.getSlackDetails().getTeamChannel());
        }

        return new AtomistTeamBase(
                teamEntity.getTeamId(),
                teamEntity.getName(),
                teamEntity.getOpenShiftCloud(),
                teamSlackIdentity);
    }

    private ActionedBy teamMemberToActionedBy(
            TeamMemberEntity teamMemberEntity) {
        TeamMemberSlackIdentity teamMemberSlackIdentity = null;
        if (teamMemberEntity.getSlackDetails() != null) {
            teamMemberSlackIdentity = new TeamMemberSlackIdentity(
                    teamMemberEntity.getSlackDetails()
                            .getScreenName(),
                    teamMemberEntity.getSlackDetails()
                            .getUserId());
        }

        return new ActionedBy(
                teamMemberEntity.getMemberId(),
                teamMemberEntity.getFirstName(),
                teamMemberEntity.getLastName(),
                teamMemberEntity.getEmail(),
                teamMemberSlackIdentity);
    }

    @Value
    private class ApplicationProdRequestCreatedWithDetails {
        private ApplicationProdRequest applicationProdRequest;

        private ApplicationCreated application;

        private AtomistProjectBase project;

        private AtomistDeploymentPipeline deploymentPipeline;

        private AtomistTeamBase owningTeam;

        private List<AtomistTeamBase> teams;

        private ActionedBy actionedBy;
    }

    @Value
    private class ApplicationProdRequest {
        private String applicationProdRequestId;

        private Date createdAt;
    }

    @Value
    private class ActionedBy {

        private String memberId;

        private String firstName;

        private String lastName;

        private String email;

        private TeamMemberSlackIdentity slackIdentity;
    }
}
