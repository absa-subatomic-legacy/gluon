package za.co.absa.subatomic.infrastructure.atomist.resource.project;

import za.co.absa.subatomic.domain.project.DeploymentEnvironment;
import za.co.absa.subatomic.domain.project.DeploymentPipeline;
import za.co.absa.subatomic.domain.project.TeamId;
import za.co.absa.subatomic.domain.project.TenantId;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.project.view.jpa.AdditionalEnvironmentEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AtomistProjectMapper {

    public AtomistAdditionalEnvironment createAtomistAdditionalEnvironment(
            AdditionalEnvironmentEntity environment) {
        return new AtomistAdditionalEnvironment.Builder()
                .displayName(environment.getDisplayName())
                .build();
    }

    public AtomistDeploymentEnvironment createAtomistDeploymentEnvironment(
            DeploymentEnvironment environment) {
        return new AtomistDeploymentEnvironment.Builder()
                .positionInPipeline(environment.getPositionInPipeline())
                .displayName(environment.getDisplayName())
                .postfix(environment.getPostfix())
                .build();
    }

    public AtomistDeploymentPipeline createAtomistDeploymentPipeline(
            DeploymentPipeline pipeline) {

        List<AtomistDeploymentEnvironment> environments = new ArrayList<>();

        if (pipeline.getEnvironments() != null) {
            for (DeploymentEnvironment environment : pipeline
                    .getEnvironments()) {
                environments
                        .add(this.createAtomistDeploymentEnvironment(
                                environment));
            }
        }

        return new AtomistDeploymentPipeline.Builder()
                .pipelineId(pipeline.getPipelineId())
                .name(pipeline.getName())
                .tag(pipeline.getTag())
                .environments(environments)
                .build();

    }

    public AtomistProject createAtomistProject(ProjectEntity projectEntity) {

        AtomistDeploymentPipeline devDeploymentPipeline = this
                .createAtomistDeploymentPipeline(
                        projectEntity.getDevDeploymentPipeline());

        List<AtomistDeploymentPipeline> releaseDeploymentPipelines = new ArrayList<>();

        if (projectEntity.getReleaseDeploymentPipelines() != null) {
            for (DeploymentPipeline pipeline : projectEntity
                    .getReleaseDeploymentPipelines()) {
                releaseDeploymentPipelines
                        .add(this.createAtomistDeploymentPipeline(pipeline));
            }
        }

        List<AtomistAdditionalEnvironment> additionalEnvironments = new ArrayList<>();
        if (projectEntity.getAdditionalEnvironmentEntities() != null) {
            additionalEnvironments = projectEntity.getAdditionalEnvironmentEntities().stream().map(
                    this::createAtomistAdditionalEnvironment)
                    .collect(Collectors.toList());
        }

        return new AtomistProject.Builder()
                .projectId(projectEntity.getProjectId())
                .name(projectEntity.getName())
                .description(projectEntity.getDescription())
                .createdBy(new TeamMemberId(
                        projectEntity.getCreatedBy().getMemberId()))
                .team(new TeamId(projectEntity.getOwningTeam().getTeamId()))
                .tenant(new TenantId(
                        projectEntity.getOwningTenant().getTenantId()))
                .devDeploymentPipeline(devDeploymentPipeline)
                .releaseDeploymentPipelines(releaseDeploymentPipelines)
                .additionalEnvironments(additionalEnvironments)
                .build();

    }

    public AtomistProjectBase createAtomistProjectBase(
            ProjectEntity projectEntity) {

        return new AtomistProjectBase.Builder()
                .projectId(projectEntity.getProjectId())
                .name(projectEntity.getName())
                .description(projectEntity.getDescription())
                .createdBy(new TeamMemberId(
                        projectEntity.getCreatedBy().getMemberId()))
                .team(new TeamId(projectEntity.getOwningTeam().getTeamId()))
                .tenant(new TenantId(
                        projectEntity.getOwningTenant().getTenantId()))
                .build();

    }
}
