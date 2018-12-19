package za.co.absa.subatomic.application.prod.generic;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.adapter.openshift.rest.OpenShiftResource;
import za.co.absa.subatomic.application.member.TeamMemberService;
import za.co.absa.subatomic.application.openshift.OpenShiftResourceService;
import za.co.absa.subatomic.application.project.ProjectService;
import za.co.absa.subatomic.application.team.TeamService;
import za.co.absa.subatomic.domain.exception.ApplicationAuthorisationException;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.openshift.view.jpa.OpenShiftResourceEntity;
import za.co.absa.subatomic.infrastructure.prod.generic.GenericProdRequestAutomationHandler;
import za.co.absa.subatomic.infrastructure.prod.generic.view.jpa.GenericProdRequestEntity;
import za.co.absa.subatomic.infrastructure.prod.generic.view.jpa.GenericProdRequestRepository;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ReleaseDeploymentPipelineEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ReleaseDeploymentPipelineRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@Service
public class GenericProdRequestService {

    private ProjectService projectService;

    private TeamMemberService teamMemberService;

    private TeamService teamService;

    private OpenShiftResourceService openShiftResourceService;

    private GenericProdRequestRepository genericProdRequestRepository;

    private GenericProdRequestAutomationHandler genericProdRequestAutomationHandler;

    private ReleaseDeploymentPipelineRepository releaseDeploymentPipelineRepository;

    public GenericProdRequestService(
            ProjectService projectService,
            TeamMemberService teamMemberService,
            TeamService teamService,
            OpenShiftResourceService openShiftResourceService,
            GenericProdRequestRepository genericProdRequestRepository,
            GenericProdRequestAutomationHandler genericProdRequestAutomationHandler,
            ReleaseDeploymentPipelineRepository releaseDeploymentPipelineRepository) {
        this.projectService = projectService;
        this.teamMemberService = teamMemberService;
        this.teamService = teamService;
        this.openShiftResourceService = openShiftResourceService;
        this.genericProdRequestRepository = genericProdRequestRepository;
        this.genericProdRequestAutomationHandler = genericProdRequestAutomationHandler;
        this.releaseDeploymentPipelineRepository = releaseDeploymentPipelineRepository;
    }

    public GenericProdRequestEntity newGenericProdRequest(
            String projectId,
            String actionedByMemberId,
            String deploymentPipelineId,
            List<OpenShiftResource> requestedResources) {

        ProjectEntity projectEntity = projectService
                .getProjectPersistenceHandler().findByProjectId(projectId);
        TeamMemberEntity actioningMember = this.teamMemberService
                .findByTeamMemberId(actionedByMemberId);
        ReleaseDeploymentPipelineEntity deploymentPipeline = this.releaseDeploymentPipelineRepository
                .findByPipelineId(deploymentPipelineId);
        Set<TeamEntity> memberAssociatedTeams = this.teamService
                .findByMemberOrOwnerMemberId(actionedByMemberId);

        if (projectEntity.getTeams().stream()
                .noneMatch(memberAssociatedTeams::contains)) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "TeamMember with id {0} is not a member of any team associated to the application {1}.",
                    actionedByMemberId, projectId));
        }

        if (projectEntity.getReleaseDeploymentPipelines()
                .stream()
                .noneMatch(deploymentPipeline::equals)) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "DeploymentPipeline with id {0} is not a valid releaseDeploymentPipeline of the project with id {1}.",
                    deploymentPipelineId, projectId));
        }

        assertMemberIsAMemberOfOwningTeam(actionedByMemberId,
                projectId, memberAssociatedTeams,
                projectEntity.getOwningTeam());

        List<OpenShiftResourceEntity> openShiftResourceEntities = this.openShiftResourceService
                .createAllOpenShiftEntities(requestedResources);

        GenericProdRequestEntity genericProdRequestEntity = GenericProdRequestEntity
                .builder()
                .genericProdRequestId(UUID.randomUUID().toString())
                .actionedBy(actioningMember)
                .openShiftResources(openShiftResourceEntities)
                .project(projectEntity)
                .projectName(projectEntity.getName())
                .build();

        GenericProdRequestEntity savedGenericProdRequestEntity = this
                .saveGenericProdRequestEntity(genericProdRequestEntity);
        if (savedGenericProdRequestEntity != null) {
            this.genericProdRequestAutomationHandler
                    .genericProdRequestCreated(savedGenericProdRequestEntity);
        }

        return savedGenericProdRequestEntity;
    }

    @Transactional(readOnly = true)
    public GenericProdRequestEntity findGenericProdRequestById(
            String id) {
        return this.genericProdRequestRepository
                .findByGenericProdRequestId(id);
    }

    @Transactional
    protected GenericProdRequestEntity saveGenericProdRequestEntity(
            GenericProdRequestEntity genericProdRequestEntity) {
        return this.genericProdRequestRepository.save(genericProdRequestEntity);
    }

    private void assertMemberIsAMemberOfOwningTeam(String memberId,
            String projectId, Collection<TeamEntity> memberAssociatedTeams,
            TeamEntity projectOwningTeam) {
        if (memberAssociatedTeams.stream()
                .noneMatch(projectOwningTeam::equals)) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "TeamMember with id {0} is not a member of the projects owning team for project with id {1}.",
                    memberId,
                    projectId));
        }
    }
}
