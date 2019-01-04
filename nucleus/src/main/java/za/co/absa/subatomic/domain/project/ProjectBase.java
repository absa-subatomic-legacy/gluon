package za.co.absa.subatomic.domain.project;

public interface ProjectBase {

    String getProjectId();

    String getName();

    String getDescription();

    String getOwningTenant();
}
