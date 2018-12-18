package za.co.absa.subatomic.infrastructure.project.view.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.project.BitbucketProject;
import za.co.absa.subatomic.domain.project.DeploymentEnvironment;
import za.co.absa.subatomic.domain.project.DeploymentPipeline;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;

@Component
public class ProjectPersistenceHandler {

    private ProjectRepository projectRepository;

    private BitbucketProjectRepository bitbucketProjectRepository;

    private DevDeploymentEnvironmentRepository devDeploymentEnvironmentRepository;

    private DevDeploymentPipelineRepository devDeploymentPipelineRepository;

    private ReleaseDeploymentEnvironmentRepository releaseDeploymentEnvironmentRepository;

    private ReleaseDeploymentPipelineRepository releaseDeploymentPipelineRepository;

    public ProjectPersistenceHandler(ProjectRepository projectRepository,
            BitbucketProjectRepository bitbucketProjectRepository,
            DevDeploymentEnvironmentRepository devDeploymentEnvironmentRepository,
            DevDeploymentPipelineRepository devDeploymentPipelineRepository,
            ReleaseDeploymentEnvironmentRepository releaseDeploymentEnvironmentRepository,
            ReleaseDeploymentPipelineRepository releaseDeploymentPipelineRepository) {
        this.projectRepository = projectRepository;
        this.bitbucketProjectRepository = bitbucketProjectRepository;
        this.devDeploymentEnvironmentRepository = devDeploymentEnvironmentRepository;
        this.devDeploymentPipelineRepository = devDeploymentPipelineRepository;
        this.releaseDeploymentEnvironmentRepository = releaseDeploymentEnvironmentRepository;
        this.releaseDeploymentPipelineRepository = releaseDeploymentPipelineRepository;
    }

    @Transactional
    public ProjectEntity createProject(String name, String description,
            TeamMemberEntity createdBy, TeamEntity owningTeam,
            TenantEntity owningTenant, DeploymentPipeline devDeploymentPipeline,
            List<? extends DeploymentPipeline> releaseDeploymentPipelines) {
        Set<TeamEntity> associatedTeams = new HashSet<>();

        associatedTeams.add(owningTeam);

        List<ReleaseDeploymentPipelineEntity> releaseDeploymentPipelineEntities = new ArrayList<>();

        for (DeploymentPipeline pipeline : releaseDeploymentPipelines) {
            releaseDeploymentPipelineEntities.add(
                    this.pipelineToReleaseDeploymentPipelineEntity(pipeline));
        }

        ProjectEntity project = ProjectEntity
                .builder()
                .projectId(UUID.randomUUID().toString())
                .name(name)
                .description(description)
                .createdBy(createdBy)
                .owningTeam(owningTeam)
                .owningTenant(owningTenant)
                .teams(associatedTeams)
                .devDeploymentPipeline(
                        this.pipelineToDevDeploymentPipelineEntity(
                                devDeploymentPipeline))
                .releaseDeploymentPipelines(releaseDeploymentPipelineEntities)
                .build();

        return this.projectRepository.save(project);
    }

    @Transactional
    public ProjectEntity linkBitbucketProject(String projectId,
            BitbucketProject bitbucketProject, TeamMemberEntity createdBy) {

        BitbucketProjectEntity bitbucketProjectEntity = BitbucketProjectEntity
                .builder()
                .bitbucketProjectId(bitbucketProject.getId())
                .url(bitbucketProject.getUrl())
                .key(bitbucketProject.getKey())
                .name(bitbucketProject.getName())
                .description(bitbucketProject.getDescription())
                .createdBy(createdBy)
                .build();

        ProjectEntity projectEntity = projectRepository
                .findByProjectId(projectId);
        projectEntity.setBitbucketProject(bitbucketProjectEntity);

        bitbucketProjectRepository.save(bitbucketProjectEntity);

        return projectRepository.save(projectEntity);
    }

    @Transactional
    public ProjectEntity linkTeamsToProject(String projectId,
            List<TeamEntity> teamEntities) {

        ProjectEntity project = projectRepository
                .findByProjectId(projectId);

        project.getTeams().addAll(teamEntities);

        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(String projectId) {
        projectRepository
                .deleteByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public ProjectEntity findByProjectId(String projectId) {
        return projectRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<ProjectEntity> findAllProjects() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ProjectEntity findByName(String name) {
        return projectRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<ProjectEntity> findByTeamName(String teamName) {
        return projectRepository.findByTeams_Name(teamName);
    }

    @Transactional(readOnly = true)
    public Collection<TeamEntity> findTeamsAssociatedToProject(
            String projectId) {
        return projectRepository.findByProjectId(projectId).getTeams();
    }

    private DevDeploymentPipelineEntity pipelineToDevDeploymentPipelineEntity(
            DeploymentPipeline deploymentPipeline) {

        DevDeploymentPipelineEntity devDeploymentPipelineEntity = DevDeploymentPipelineEntity
                .builder().build();

        devDeploymentPipelineRepository.save(devDeploymentPipelineEntity);
        devDeploymentPipelineEntity.setEnvironments(
                this.environmentsToDevDeploymentEnvironmentEntity(
                        deploymentPipeline.getEnvironments(),
                        devDeploymentPipelineEntity));
        devDeploymentPipelineRepository.save(devDeploymentPipelineEntity);

        return devDeploymentPipelineEntity;
    }

    private List<DevDeploymentEnvironmentEntity> environmentsToDevDeploymentEnvironmentEntity(
            List<? extends DeploymentEnvironment> deploymentEnvironments,
            DevDeploymentPipelineEntity owningPipeline) {
        List<DevDeploymentEnvironmentEntity> environments = new ArrayList<>();
        if (deploymentEnvironments != null) {
            for (DeploymentEnvironment environment : deploymentEnvironments) {
                DevDeploymentEnvironmentEntity environmentEntity = DevDeploymentEnvironmentEntity
                        .builder()
                        .positionInPipeline(environment.getPositionInPipeline())
                        .displayName(environment.getDisplayName())
                        .prefix(environment.getPrefix())
                        .pipeline(owningPipeline)
                        .build();
                this.devDeploymentEnvironmentRepository.save(environmentEntity);
                environments.add(environmentEntity);
            }
        }
        return environments;
    }

    private ReleaseDeploymentPipelineEntity pipelineToReleaseDeploymentPipelineEntity(
            DeploymentPipeline deploymentPipeline) {

        ReleaseDeploymentPipelineEntity devDeploymentPipelineEntity = ReleaseDeploymentPipelineEntity
                .builder().build();

        releaseDeploymentPipelineRepository.save(devDeploymentPipelineEntity);
        devDeploymentPipelineEntity.setEnvironments(
                this.environmentsToReleaseDeploymentEnvironmentEntity(
                        deploymentPipeline.getEnvironments(),
                        devDeploymentPipelineEntity));
        releaseDeploymentPipelineRepository.save(devDeploymentPipelineEntity);

        return devDeploymentPipelineEntity;
    }

    private List<ReleaseDeploymentEnvironmentEntity> environmentsToReleaseDeploymentEnvironmentEntity(
            List<? extends DeploymentEnvironment> deploymentEnvironments,
            ReleaseDeploymentPipelineEntity owningPipeline) {
        List<ReleaseDeploymentEnvironmentEntity> environments = new ArrayList<>();
        if (deploymentEnvironments != null) {
            for (DeploymentEnvironment environment : deploymentEnvironments) {
                ReleaseDeploymentEnvironmentEntity environmentEntity = ReleaseDeploymentEnvironmentEntity
                        .builder()
                        .positionInPipeline(environment.getPositionInPipeline())
                        .displayName(environment.getDisplayName())
                        .prefix(environment.getPrefix())
                        .pipeline(owningPipeline)
                        .build();
                this.releaseDeploymentEnvironmentRepository
                        .save(environmentEntity);
                environments.add(environmentEntity);
            }
        }
        return environments;
    }
}
