package za.co.absa.subatomic.adapter.project.rest;

import java.util.List;

import lombok.Data;
import za.co.absa.subatomic.domain.project.DeploymentPipeline;

@Data
class DeploymentPipelineResource
        implements DeploymentPipeline {

    private String name;

    private List<DeploymentEnvironmentResource> environments;
}
