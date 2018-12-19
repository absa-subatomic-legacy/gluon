package za.co.absa.subatomic.infrastructure.atomist.resource.project;

import java.util.ArrayList;
import java.util.List;

import za.co.absa.subatomic.domain.project.DeploymentEnvironment;
import za.co.absa.subatomic.domain.project.DeploymentPipeline;
import za.co.absa.subatomic.domain.project.TeamId;
import za.co.absa.subatomic.domain.project.TenantId;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;

public class AtomistProjectMapper {

    public AtomistDeploymentEnvironment createAtomistDeploymentEnvironment(
            DeploymentEnvironment environment) {
        return new AtomistDeploymentEnvironment.Builder()
                .positionInPipeline(environment.getPositionInPipeline())
                .displayName(environment.getDisplayName())
                .prefix(environment.getPrefix())
                .build();
    }

    public AtomistDeploymentPipeline createAtomistDeploymentPipeline(
            DeploymentPipeline pipeline) {

        List<AtomistDeploymentEnvironment> environments = new ArrayList<>();

        for (DeploymentEnvironment environment : pipeline.getEnvironments()) {
            environments
                    .add(this.createAtomistDeploymentEnvironment(environment));
        }

        return new AtomistDeploymentPipeline.Builder()
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

        for (DeploymentPipeline pipeline : projectEntity
                .getReleaseDeploymentPipelines()) {
            releaseDeploymentPipelines
                    .add(this.createAtomistDeploymentPipeline(pipeline));
        }

        return new AtomistProject.Builder()
                .projectId(projectEntity.getProjectId())
                .name(projectEntity.getName())
                .description(projectEntity.getDescription())
                .createdBy(new TeamMemberId(projectEntity.getProjectId()))
                .team(new TeamId(projectEntity.getOwningTeam().getTeamId()))
                .tenant(new TenantId(
                        projectEntity.getOwningTenant().getTenantId()))
                .devDeploymentPipeline(devDeploymentPipeline)
                .releaseDeploymentPipelines(releaseDeploymentPipelines)
                .build();

    }

    public AtomistProjectBase createAtomistProjectBase(
            ProjectEntity projectEntity) {

        return new AtomistProjectBase.Builder()
                .projectId(projectEntity.getProjectId())
                .name(projectEntity.getName())
                .description(projectEntity.getDescription())
                .createdBy(new TeamMemberId(projectEntity.getProjectId()))
                .team(new TeamId(projectEntity.getOwningTeam().getTeamId()))
                .tenant(new TenantId(
                        projectEntity.getOwningTenant().getTenantId()))
                .build();

    }
}
