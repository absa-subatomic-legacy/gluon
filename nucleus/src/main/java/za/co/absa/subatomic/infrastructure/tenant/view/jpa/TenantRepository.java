package za.co.absa.subatomic.infrastructure.tenant.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<TenantEntity, Long> {

    TenantEntity findByTenantId(String tenantId);

    TenantEntity findByName(String name);
}
