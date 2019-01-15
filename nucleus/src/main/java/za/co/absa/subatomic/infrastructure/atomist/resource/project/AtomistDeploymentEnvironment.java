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

        private String postfix;

        public Builder positionInPipeline(final int positionInPipeline) {
            this.positionInPipeline = positionInPipeline;
            return this;
        }

        public Builder displayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder postfix(final String postfix) {
            this.postfix = postfix;
            return this;
        }

        public AtomistDeploymentEnvironment build() {
            AtomistDeploymentEnvironment atomistDeploymentEnvironment = new AtomistDeploymentEnvironment();
            atomistDeploymentEnvironment
                    .setPositionInPipeline(this.positionInPipeline);
            atomistDeploymentEnvironment.setDisplayName(this.displayName);
            atomistDeploymentEnvironment.setPostfix(this.postfix);
            return atomistDeploymentEnvironment;
        }

    }

    private int positionInPipeline;

    private String displayName;

    private String postfix;

}
