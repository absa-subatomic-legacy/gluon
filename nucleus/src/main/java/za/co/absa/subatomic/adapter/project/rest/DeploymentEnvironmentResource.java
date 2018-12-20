package za.co.absa.subatomic.adapter.project.rest;

import lombok.Data;
import za.co.absa.subatomic.domain.project.DeploymentEnvironment;

@Data
class DeploymentEnvironmentResource
        implements DeploymentEnvironment {

    private int positionInPipeline;

    private String displayName;

    private String postfix;
}
