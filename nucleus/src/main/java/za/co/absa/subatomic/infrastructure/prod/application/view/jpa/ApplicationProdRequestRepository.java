package za.co.absa.subatomic.infrastructure.prod.application.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationProdRequestRepository
        extends JpaRepository<ApplicationProdRequestEntity, Long> {

    ApplicationProdRequestEntity findByApplicationProdRequestId(String id);
}
