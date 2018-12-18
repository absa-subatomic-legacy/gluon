package za.co.absa.subatomic.domain.project;

import java.util.List;

public interface DeploymentPipeline {

    String getName();

    List<DeploymentEnvironment> getEnvironments();

}
