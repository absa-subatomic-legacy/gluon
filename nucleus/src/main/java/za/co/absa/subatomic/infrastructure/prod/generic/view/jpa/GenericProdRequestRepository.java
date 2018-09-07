package za.co.absa.subatomic.infrastructure.prod.generic.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GenericProdRequestRepository
        extends JpaRepository<GenericProdRequestEntity, Long> {

    GenericProdRequestEntity findByGenericProdRequestId(String id);
}
