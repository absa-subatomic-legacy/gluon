package za.co.absa.subatomic.adapter.project.rest;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import za.co.absa.subatomic.domain.project.DeploymentPipeline;

@Setter
@Getter
class DeploymentPipelineResource
        implements DeploymentPipeline {

    private String pipelineId;

    private String name;

    private String tag;

    private List<DeploymentEnvironmentResource> environments;
}
