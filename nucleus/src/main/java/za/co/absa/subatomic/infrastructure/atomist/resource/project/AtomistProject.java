package za.co.absa.subatomic.infrastructure.atomist.resource.project;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import za.co.absa.subatomic.domain.project.TeamId;
import za.co.absa.subatomic.domain.project.TenantId;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Getter
@Setter
public class AtomistProject extends AtomistProjectBase {
    public static class Builder {

        private String projectId;

        private String name;

        private String description;

        private TeamMemberId createdBy;

        private TeamId team;

        private TenantId tenant;

        private AtomistDeploymentPipeline devDeploymentPipeline;

        private List<AtomistDeploymentPipeline> releaseDeploymentPipelines;

        public Builder projectId(final String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder description(
                final String description) {
            this.description = description;
            return this;
        }

        public Builder createdBy(
                final TeamMemberId createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder team(final TeamId team) {
            this.team = team;
            return this;
        }

        public Builder tenant(final TenantId tenant) {
            this.tenant = tenant;
            return this;
        }

        public Builder devDeploymentPipeline(
                final AtomistDeploymentPipeline devDeploymentPipeline) {
            this.devDeploymentPipeline = devDeploymentPipeline;
            return this;
        }

        public Builder releaseDeploymentPipelines(
                final List<AtomistDeploymentPipeline> releaseDeploymentPipelines) {
            this.releaseDeploymentPipelines = releaseDeploymentPipelines;
            return this;
        }

        public AtomistProject build() {
            AtomistProject project = new AtomistProject();
            project.setProjectId(this.projectId);
            project.setName(this.name);
            project.setDescription(this.description);
            project.setCreatedBy(this.createdBy);
            project.setTeam(this.team);
            project.setTenant(this.tenant);
            project.setDevDeploymentPipeline(this.devDeploymentPipeline);
            project.setReleaseDeploymentPipelines(
                    this.releaseDeploymentPipelines);
            return project;
        }

    }

    private AtomistDeploymentPipeline devDeploymentPipeline;

    private List<AtomistDeploymentPipeline> releaseDeploymentPipelines;
}
