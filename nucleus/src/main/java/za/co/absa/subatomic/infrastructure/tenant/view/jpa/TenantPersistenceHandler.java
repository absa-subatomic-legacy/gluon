package za.co.absa.subatomic.infrastructure.tenant.view.jpa;

import java.util.UUID;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.tenant.TenantCreated;

@Component
public class TenantPersistenceHandler {

    private TenantRepository tenantRepository;

    public TenantPersistenceHandler(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @EventHandler
    @Transactional
    void on(TenantCreated event) {

        TenantEntity tenantEntity = TenantEntity.builder()
                .tenantId(event.getTenantId())
                .name(event.getName())
                .description(event.getDescription())
                .build();

        tenantRepository.save(tenantEntity);
    }

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
