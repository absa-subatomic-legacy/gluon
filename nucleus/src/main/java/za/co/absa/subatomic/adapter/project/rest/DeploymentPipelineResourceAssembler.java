package za.co.absa.subatomic.adapter.project.rest;

import java.util.ArrayList;
import java.util.List;

import za.co.absa.subatomic.domain.project.DeploymentEnvironment;
import za.co.absa.subatomic.domain.project.DeploymentPipeline;

public class DeploymentPipelineResourceAssembler {

    public DeploymentPipelineResource toResource(DeploymentPipeline pipeline) {
        DeploymentPipelineResource deploymentPipelineResource = new DeploymentPipelineResource();
        deploymentPipelineResource.setPipelineId(pipeline.getPipelineId());
        deploymentPipelineResource.setName(pipeline.getName());
        deploymentPipelineResource.setTag(pipeline.getTag());

        List<DeploymentEnvironmentResource> environments = new ArrayList<>();

        for (DeploymentEnvironment environment : pipeline.getEnvironments()) {
            environments.add(this.toDeploymentEnvironmentResource(environment));
        }

        deploymentPipelineResource.setEnvironments(environments);

        return deploymentPipelineResource;
    }

    private DeploymentEnvironmentResource toDeploymentEnvironmentResource(
            DeploymentEnvironment environment) {
        DeploymentEnvironmentResource deploymentEnvironmentResource = new DeploymentEnvironmentResource();
        deploymentEnvironmentResource
                .setPositionInPipeline(environment.getPositionInPipeline());
        deploymentEnvironmentResource
                .setDisplayName(environment.getDisplayName());
        deploymentEnvironmentResource.setPrefix(environment.getPrefix());
        return deploymentEnvironmentResource;
    }
}
