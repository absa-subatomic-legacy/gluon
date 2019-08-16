package za.co.absa.subatomic.infrastructure.atomist.resource.project;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtomistAdditionalEnvironment {

    public static class Builder {
        private String displayName;

        public Builder displayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        public AtomistAdditionalEnvironment build() {
            AtomistAdditionalEnvironment atomistDeploymentEnvironment = new AtomistAdditionalEnvironment();
            atomistDeploymentEnvironment.setDisplayName(this.displayName);
            return atomistDeploymentEnvironment;
        }

    }

    private String displayName;

}
