package za.co.absa.subatomic.infrastructure.application.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository
        extends JpaRepository<ApplicationEntity, Long> {

    ApplicationEntity findByApplicationId(String applicationId);

    ApplicationEntity findByName(String name);
}
