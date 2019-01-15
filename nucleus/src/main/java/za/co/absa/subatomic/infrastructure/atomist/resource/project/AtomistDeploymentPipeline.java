package za.co.absa.subatomic.infrastructure.atomist.resource.project;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import za.co.absa.subatomic.domain.project.DeploymentPipeline;

@Setter
@Getter
public class AtomistDeploymentPipeline implements DeploymentPipeline {

    public static class Builder {

        private String pipelineId;

        private String name;

        private String tag;

        private List<AtomistDeploymentEnvironment> environments;

        public Builder pipelineId(final String pipelineId) {
            this.pipelineId = pipelineId;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder tag(final String tag) {
            this.tag = tag;
            return this;
        }

        public Builder environments(
                final List<AtomistDeploymentEnvironment> environments) {
            this.environments = environments;
            return this;
        }

        public AtomistDeploymentPipeline build() {
            AtomistDeploymentPipeline atomistDeploymentPipeline = new AtomistDeploymentPipeline();
            atomistDeploymentPipeline.setPipelineId(this.pipelineId);
            atomistDeploymentPipeline.setName(this.name);
            atomistDeploymentPipeline.setTag(this.tag);
            atomistDeploymentPipeline.setEnvironments(this.environments);
            return atomistDeploymentPipeline;
        }

    }

    private String pipelineId;

    private String name;

    private String tag;

    private List<AtomistDeploymentEnvironment> environments;
}
