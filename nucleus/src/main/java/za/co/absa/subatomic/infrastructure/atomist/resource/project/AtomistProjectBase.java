package za.co.absa.subatomic.infrastructure.atomist.resource.project;

import lombok.Getter;
import lombok.Setter;
import za.co.absa.subatomic.domain.project.TeamId;
import za.co.absa.subatomic.domain.project.TenantId;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Getter
@Setter
public class AtomistProjectBase {

    public static class Builder {

        private String projectId;

        private String name;

        private String description;

        private TeamMemberId createdBy;

        private TeamId team;

        private TenantId tenant;

        public Builder projectId(final String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public Builder createdBy(final TeamMemberId createdBy) {
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

        public AtomistProjectBase build() {
            AtomistProjectBase projectBase = new AtomistProjectBase();
            projectBase.setProjectId(this.projectId);
            projectBase.setName(this.name);
            projectBase.setDescription(this.description);
            projectBase.setCreatedBy(this.createdBy);
            projectBase.setTeam(this.team);
            projectBase.setTenant(this.tenant);
            return projectBase;
        }

    }

    private String projectId;

    private String name;

    private String description;

    private TeamMemberId createdBy;

    private TeamId team;

    private TenantId tenant;
}
