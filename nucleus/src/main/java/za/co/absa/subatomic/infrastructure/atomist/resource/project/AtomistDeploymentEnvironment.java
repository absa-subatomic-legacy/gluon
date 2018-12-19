package za.co.absa.subatomic.infrastructure.atomist.resource.project;

import lombok.Getter;
import lombok.Setter;
import za.co.absa.subatomic.domain.project.DeploymentEnvironment;

@Getter
@Setter
public class AtomistDeploymentEnvironment implements DeploymentEnvironment {

    public static class Builder {
        private int positionInPipeline;

        private String displayName;

        private String prefix;

        public Builder positionInPipeline(final int positionInPipeline) {
            this.positionInPipeline = positionInPipeline;
            return this;
        }

        public Builder displayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder prefix(final String prefix) {
            this.prefix = prefix;
            return this;
        }

        public AtomistDeploymentEnvironment build() {
            AtomistDeploymentEnvironment atomistDeploymentEnvironment = new AtomistDeploymentEnvironment();
            atomistDeploymentEnvironment
                    .setPositionInPipeline(this.positionInPipeline);
            atomistDeploymentEnvironment.setDisplayName(this.displayName);
            atomistDeploymentEnvironment.setPrefix(this.prefix);
            return atomistDeploymentEnvironment;
        }

    }

    private int positionInPipeline;

    private String displayName;

    private String prefix;

}
