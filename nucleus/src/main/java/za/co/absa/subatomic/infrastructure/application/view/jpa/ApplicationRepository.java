package za.co.absa.subatomic.infrastructure.application.view.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository
        extends JpaRepository<ApplicationEntity, Long> {

    ApplicationEntity findByApplicationId(String applicationId);

    ApplicationEntity findByName(String name);

    List<ApplicationEntity> findByProjectName(String projectName);
}
