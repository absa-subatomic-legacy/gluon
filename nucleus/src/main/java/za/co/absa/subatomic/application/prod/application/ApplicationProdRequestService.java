package za.co.absa.subatomic.application.prod.application;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.adapter.openshift.rest.OpenShiftResource;
import za.co.absa.subatomic.application.application.ApplicationService;
import za.co.absa.subatomic.application.member.TeamMemberService;
import za.co.absa.subatomic.application.openshift.OpenShiftResourceService;
import za.co.absa.subatomic.application.team.TeamService;
import za.co.absa.subatomic.domain.exception.ApplicationAuthorisationException;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.openshift.view.jpa.OpenShiftResourceEntity;
import za.co.absa.subatomic.infrastructure.prod.application.ApplicationProdRequestAutomationHandler;
import za.co.absa.subatomic.infrastructure.prod.application.view.jpa.ApplicationProdRequestEntity;
import za.co.absa.subatomic.infrastructure.prod.application.view.jpa.ApplicationProdRequestRepository;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ReleaseDeploymentPipelineEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ReleaseDeploymentPipelineRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@Service
public class ApplicationProdRequestService {

    private ApplicationService applicationService;

    private TeamMemberService teamMemberService;

    private TeamService teamService;

    private OpenShiftResourceService openShiftResourceService;

    private ApplicationProdRequestRepository applicationProdRequestRepository;

    private ApplicationProdRequestAutomationHandler applicationProdRequestAutomationHandler;

    private ReleaseDeploymentPipelineRepository releaseDeploymentPipelineRepository;

    public ApplicationProdRequestService(
            ApplicationService applicationService,
            TeamMemberService teamMemberService,
            TeamService teamService,
            OpenShiftResourceService openShiftResourceService,
            ApplicationProdRequestRepository applicationProdRequestRepository,
            ApplicationProdRequestAutomationHandler applicationProdRequestAutomationHandler,
            ReleaseDeploymentPipelineRepository releaseDeploymentPipelineRepository) {
        this.applicationService = applicationService;
        this.teamMemberService = teamMemberService;
        this.teamService = teamService;
        this.openShiftResourceService = openShiftResourceService;
        this.applicationProdRequestRepository = applicationProdRequestRepository;
        this.applicationProdRequestAutomationHandler = applicationProdRequestAutomationHandler;
        this.releaseDeploymentPipelineRepository = releaseDeploymentPipelineRepository;
    }

    @Transactional
    public ApplicationProdRequestEntity newApplicationProdRequest(
            String applicationId,
            String actionedByMemberId,
            String deploymentPipelineId,
            List<OpenShiftResource> requestedResources) {
        ApplicationEntity applicationEntity = this.applicationService
                .findByApplictionId(applicationId);
        TeamMemberEntity actionedBy = this.teamMemberService
                .findByTeamMemberId(actionedByMemberId);
        ReleaseDeploymentPipelineEntity deploymentPipeline = this.releaseDeploymentPipelineRepository
                .findByPipelineId(deploymentPipelineId);
        Set<TeamEntity> memberAssociatedTeams = this.teamService
                .findByMemberOrOwnerMemberId(actionedByMemberId);

        if (applicationEntity.getProject().getTeams().stream()
                .noneMatch(memberAssociatedTeams::contains)) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "TeamMember with id {0} is not a member of any team associated to the application {1}.",
                    actionedByMemberId, applicationId));
        }

        if (applicationEntity.getProject().getReleaseDeploymentPipelines()
                .stream()
                .noneMatch(deploymentPipeline::equals)) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "DeploymentPipeline with id {0} is not a valid releaseDeploymentPipeline of the project owning the the application {1}.",
                    deploymentPipelineId, applicationId));
        }

        List<OpenShiftResourceEntity> openShiftResourceEntities = this.openShiftResourceService
                .createAllOpenShiftEntities(requestedResources);

        ApplicationProdRequestEntity applicationProdRequestEntity = ApplicationProdRequestEntity
                .builder()
                .applicationProdRequestId(UUID.randomUUID().toString())
                .application(applicationEntity)
                .applicationName(applicationEntity.getName())
                .deploymentPipeline(deploymentPipeline)
                .projectName(applicationEntity.getProject().getName())
                .actionedBy(actionedBy)
                .openShiftResources(openShiftResourceEntities)
                .build();

        ApplicationProdRequestEntity applicationProdRequestResult = this.applicationProdRequestRepository
                .save(applicationProdRequestEntity);

        if (applicationProdRequestResult != null) {
            this.applicationProdRequestAutomationHandler
                    .applicationProdRequestCreated(
                            applicationProdRequestResult);
        }

        return applicationProdRequestResult;
    }

    @Transactional(readOnly = true)
    public ApplicationProdRequestEntity findApplicationProjectRequestById(
            String id) {
        return this.applicationProdRequestRepository
                .findByApplicationProdRequestId(id);
    }
}
