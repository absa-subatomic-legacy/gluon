package za.co.absa.subatomic.infrastructure.application.view.jpa;

import java.util.List;

import za.co.absa.subatomic.domain.application.ApplicationType;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository
        extends JpaRepository<ApplicationEntity, Long> {

    ApplicationEntity findByApplicationId(String applicationId);

    List<ApplicationEntity> findByName(String name);

    List<ApplicationEntity> findByProjectName(String projectName);

    List<ApplicationEntity> findByApplicationType(
            ApplicationType applicationType);

    List<ApplicationEntity> findByProjectProjectId(String projectId);

    ApplicationEntity findByNameAndProjectName(String name, String projectName);

    ApplicationEntity findByNameAndProjectId(String name, Long projectId);

    void deleteByApplicationId(String applicationId);
}
