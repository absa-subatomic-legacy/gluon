package za.co.absa.subatomic.domain.project;

import java.util.List;

public interface DeploymentPipeline {

    String getPipelineId();

    String getName();

    String getTag();

    List<? extends DeploymentEnvironment> getEnvironments();

}
