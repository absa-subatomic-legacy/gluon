package za.co.absa.subatomic.domain.project;

public interface DeploymentEnvironment {

    int getPositionInPipeline();

    String getDisplayName();

    String getPrefix();

}
