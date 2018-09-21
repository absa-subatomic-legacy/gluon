package za.co.absa.subatomic.infrastructure.tenant.view.jpa;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TenantPersistenceHandler {

    private TenantRepository tenantRepository;

    public TenantPersistenceHandler(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public TenantEntity createTenant(String name,
            String description) {
        TenantEntity tenantEntity = TenantEntity.builder()
                .tenantId(UUID.randomUUID().toString())
                .name(name)
                .description(description)
                .build();

        return tenantRepository.save(tenantEntity);
    }
}
