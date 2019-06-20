package za.co.absa.subatomic.infrastructure.project.view.jpa;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.absa.subatomic.domain.project.BitbucketProject;
import za.co.absa.subatomic.domain.project.DeploymentEnvironment;
import za.co.absa.subatomic.domain.project.DeploymentPipeline;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    public ProjectEntity unlinkTeamsFromProject(String projectId,
                                                List<TeamEntity> teamEntities) {

        ProjectEntity project = projectRepository
                .findByProjectId(projectId);

        return this.unlinkTeamsFromProject(project, teamEntities);
    }

    @Transactional
    public ProjectEntity unlinkTeamsFromProject(ProjectEntity project,
                                                List<TeamEntity> teamEntities) {

        project.getTeams().removeAll(teamEntities);

        return projectRepository.save(project);
    }

    @Transactional
    public ProjectEntity updateDevDeploymentPipeline(String projectId,
                                                     DeploymentPipeline updatedPipeline) {
        ProjectEntity project = projectRepository
                .findByProjectId(projectId);

        DevDeploymentPipelineEntity devDeploymentPipeline = project
                .getDevDeploymentPipeline();

        List<DevDeploymentEnvironmentEntity> environments = new ArrayList<>();

        List<DevDeploymentEnvironmentEntity> originalEnvironments = devDeploymentPipeline
                .getEnvironments();

        if (originalEnvironments == null) {
            originalEnvironments = new ArrayList<>();
        }

        for (DeploymentEnvironment updatedEnvironment : updatedPipeline
                .getEnvironments()) {
            // Try match the updatedEnvironment to an existing one. If matched the environment is
            // removed from the supplied environment list
            DevDeploymentEnvironmentEntity environmentEntity = extractAndMergeExistingDevEnvironment(
                    originalEnvironments, updatedEnvironment);
            // If environment did not exist, create a new one.
            if (environmentEntity == null) {
                environmentEntity = this
                        .environmentToDevDeploymentEnvironmentEntity(
                                updatedEnvironment, devDeploymentPipeline);
            }

            // Set the position based on the newly provided position
            environmentEntity
                    .setPositionInPipeline(environments.size() + 1);

            environments.add(environmentEntity);

        }

        this.devDeploymentEnvironmentRepository.saveAll(environments);
        devDeploymentPipeline.setEnvironments(environments);
        this.devDeploymentPipelineRepository.save(devDeploymentPipeline);

        // Delete the remaining unmatched original environments as they are considered removed now
        this.devDeploymentEnvironmentRepository.deleteAll(originalEnvironments);

        return project;
    }

    @Transactional
    public ProjectEntity updateReleaseDeploymentPipelines(String projectId,
                                                          List<? extends DeploymentPipeline> updatedPipelines) {
        ProjectEntity project = projectRepository
                .findByProjectId(projectId);

        List<ReleaseDeploymentPipelineEntity> pipelines = new ArrayList<>();

        List<ReleaseDeploymentPipelineEntity> originalPipelines = project
                .getReleaseDeploymentPipelines();

        if (originalPipelines == null) {
            originalPipelines = new ArrayList<>();
        }

        for (DeploymentPipeline updatedPipeline : updatedPipelines) {
            // Try match the updatedPipeline to an existing one. If matched the environment is
            // removed from the supplied pipeline list
            ReleaseDeploymentPipelineEntity pipelineEntity = extractAndMergeExistingReleasePipeline(
                    originalPipelines, updatedPipeline);
            // If pipeline did not exist, create a new one.
            if (pipelineEntity == null) {
                pipelineEntity = this
                        .pipelineToReleaseDeploymentPipelineEntity(
                                updatedPipeline);
            } else if (updatedPipeline.getEnvironments() != null) {
                // Update the existing deployment pipeline environments
                pipelineEntity = this.updateReleaseDeploymentPipeline(
                        pipelineEntity,
                        updatedPipeline);
            }

            pipelines.add(pipelineEntity);
        }

        this.releaseDeploymentPipelineRepository.saveAll(pipelines);
        project.setReleaseDeploymentPipelines(pipelines);
        this.projectRepository.save(project);

        // Delete the remaining unmatched original pipelines as they are considered removed now
        this.releaseDeploymentPipelineRepository.deleteAll(originalPipelines);

        return project;
    }

    @Transactional
    public ReleaseDeploymentPipelineEntity updateReleaseDeploymentPipeline(
            ReleaseDeploymentPipelineEntity releaseDeploymentPipelineEntity,
            DeploymentPipeline updatedPipeline) {

        List<ReleaseDeploymentEnvironmentEntity> environments = new ArrayList<>();

        List<ReleaseDeploymentEnvironmentEntity> originalEnvironments = releaseDeploymentPipelineEntity
                .getEnvironments();

        if (originalEnvironments == null) {
            originalEnvironments = new ArrayList<>();
        }

        for (DeploymentEnvironment updatedEnvironment : updatedPipeline
                .getEnvironments()) {
            // Try match the updatedEnvironment to an existing one. If matched the environment is
            // removed from the supplied environment list
            ReleaseDeploymentEnvironmentEntity environmentEntity = extractAndMergeExistingReleaseEnvironment(
                    originalEnvironments, updatedEnvironment);
            // If environment did not exist, create a new one.
            if (environmentEntity == null) {
                environmentEntity = this
                        .environmentToReleaseDeploymentEnvironmentEntity(
                                updatedEnvironment,
                                releaseDeploymentPipelineEntity);
            }

            // Set the position based on the newly provided position
            environmentEntity
                    .setPositionInPipeline(environments.size() + 1);

            environments.add(environmentEntity);

        }

        this.releaseDeploymentEnvironmentRepository.saveAll(environments);
        releaseDeploymentPipelineEntity.setEnvironments(environments);
        this.releaseDeploymentPipelineRepository
                .save(releaseDeploymentPipelineEntity);

        // Delete the remaining unmatched original environments as they are considered removed now
        this.releaseDeploymentEnvironmentRepository
                .deleteAll(originalEnvironments);

        return releaseDeploymentPipelineEntity;
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
        DevDeploymentPipelineEntity.DevDeploymentPipelineEntityBuilder devDeploymentPipelineEntityBuilder = DevDeploymentPipelineEntity
                .builder().pipelineId(UUID.randomUUID().toString());
        DevDeploymentPipelineEntity devDeploymentPipelineEntity;
        if (deploymentPipeline != null) {

            devDeploymentPipelineEntity = devDeploymentPipelineEntityBuilder
                    .name(deploymentPipeline.getName()).build();

            devDeploymentPipelineRepository.save(devDeploymentPipelineEntity);
            devDeploymentPipelineEntity.setEnvironments(
                    this.environmentsToDevDeploymentEnvironmentEntity(
                            deploymentPipeline.getEnvironments(),
                            devDeploymentPipelineEntity));
            devDeploymentPipelineRepository.save(devDeploymentPipelineEntity);
        } else {
            devDeploymentPipelineEntity = devDeploymentPipelineEntityBuilder
                    .build();
            devDeploymentPipelineRepository.save(devDeploymentPipelineEntity);
        }

        return devDeploymentPipelineEntity;
    }

    private List<DevDeploymentEnvironmentEntity> environmentsToDevDeploymentEnvironmentEntity(
            List<? extends DeploymentEnvironment> deploymentEnvironments,
            DevDeploymentPipelineEntity owningPipeline) {
        List<DevDeploymentEnvironmentEntity> environments = new ArrayList<>();
        if (deploymentEnvironments != null) {
            for (DeploymentEnvironment environment : deploymentEnvironments) {
                DevDeploymentEnvironmentEntity environmentEntity = this
                        .environmentToDevDeploymentEnvironmentEntity(
                                environment, owningPipeline);
                this.devDeploymentEnvironmentRepository.save(environmentEntity);
                environments.add(environmentEntity);
            }
        }
        return environments;
    }

    private DevDeploymentEnvironmentEntity environmentToDevDeploymentEnvironmentEntity(
            DeploymentEnvironment environment,
            DevDeploymentPipelineEntity owningPipeline) {
        return DevDeploymentEnvironmentEntity
                .builder()
                .positionInPipeline(environment.getPositionInPipeline())
                .displayName(environment.getDisplayName())
                .postfix(environment.getPostfix())
                .pipeline(owningPipeline)
                .build();
    }

    private ReleaseDeploymentEnvironmentEntity environmentToReleaseDeploymentEnvironmentEntity(
            DeploymentEnvironment environment,
            ReleaseDeploymentPipelineEntity owningPipeline) {
        return ReleaseDeploymentEnvironmentEntity
                .builder()
                .positionInPipeline(environment.getPositionInPipeline())
                .displayName(environment.getDisplayName())
                .postfix(environment.getPostfix())
                .pipeline(owningPipeline)
                .build();
    }

    private ReleaseDeploymentPipelineEntity pipelineToReleaseDeploymentPipelineEntity(
            DeploymentPipeline deploymentPipeline) {

        ReleaseDeploymentPipelineEntity devDeploymentPipelineEntity = ReleaseDeploymentPipelineEntity
                .builder().pipelineId(UUID.randomUUID().toString())
                .name(deploymentPipeline.getName())
                .tag(deploymentPipeline.getTag()).build();

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
                        .postfix(environment.getPostfix())
                        .pipeline(owningPipeline)
                        .build();
                this.releaseDeploymentEnvironmentRepository
                        .save(environmentEntity);
                environments.add(environmentEntity);
            }
        }
        return environments;
    }

    private DevDeploymentEnvironmentEntity extractAndMergeExistingDevEnvironment(
            List<DevDeploymentEnvironmentEntity> devDeploymentEnvironmentEntities,
            DeploymentEnvironment updatedEnvironment) {

        DevDeploymentEnvironmentEntity environmentEntity = null;
        for (int index = devDeploymentEnvironmentEntities.size()
                - 1; index >= 0; index--) {
            DevDeploymentEnvironmentEntity deploymentEnvironmentEntity = devDeploymentEnvironmentEntities
                    .get(index);
            if (deploymentEnvironmentEntity.getPostfix()
                    .equals(updatedEnvironment.getPostfix())) {
                environmentEntity = deploymentEnvironmentEntity;
                environmentEntity.setDisplayName(
                        updatedEnvironment.getDisplayName());
                devDeploymentEnvironmentEntities.remove(index);
                break;
            }
        }
        return environmentEntity;
    }

    private ReleaseDeploymentEnvironmentEntity extractAndMergeExistingReleaseEnvironment(
            List<ReleaseDeploymentEnvironmentEntity> releaseDeploymentEnvironmentEntities,
            DeploymentEnvironment updatedEnvironment) {

        ReleaseDeploymentEnvironmentEntity environmentEntity = null;
        for (int index = releaseDeploymentEnvironmentEntities.size()
                - 1; index >= 0; index--) {
            ReleaseDeploymentEnvironmentEntity deploymentEnvironmentEntity = releaseDeploymentEnvironmentEntities
                    .get(index);
            if (deploymentEnvironmentEntity.getPostfix()
                    .equals(updatedEnvironment.getPostfix())) {
                environmentEntity = deploymentEnvironmentEntity;
                environmentEntity.setDisplayName(
                        updatedEnvironment.getDisplayName());
                releaseDeploymentEnvironmentEntities.remove(index);
                break;
            }
        }
        return environmentEntity;
    }

    private ReleaseDeploymentPipelineEntity extractAndMergeExistingReleasePipeline(
            List<ReleaseDeploymentPipelineEntity> releaseDeploymentPipelineEntities,
            DeploymentPipeline updatedPipeline) {

        ReleaseDeploymentPipelineEntity pipelineEntity = null;
        for (int index = releaseDeploymentPipelineEntities.size()
                - 1; index >= 0; index--) {
            ReleaseDeploymentPipelineEntity releaseDeploymentPipelineEntity = releaseDeploymentPipelineEntities
                    .get(index);
            if (releaseDeploymentPipelineEntity.getPipelineId()
                    .equals(updatedPipeline.getPipelineId())
                    || releaseDeploymentPipelineEntity.getTag()
                    .equals(updatedPipeline.getTag())) {
                pipelineEntity = releaseDeploymentPipelineEntity;
                pipelineEntity.setName(
                        updatedPipeline.getName());
                pipelineEntity.setTag(updatedPipeline.getTag());
                releaseDeploymentPipelineEntities.remove(index);
                break;
            }
        }
        return pipelineEntity;
    }
}
