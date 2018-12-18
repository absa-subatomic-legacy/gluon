package za.co.absa.subatomic.adapter.project.rest;

import java.util.List;

import lombok.Setter;
import za.co.absa.subatomic.domain.project.DeploymentPipeline;

@Setter
class DeploymentPipelineResource
        implements DeploymentPipeline {

    private String name;

    private List<DeploymentEnvironmentResource> environments;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<DeploymentEnvironmentResource> getEnvironments() {
        return this.environments;
    }
}
