package za.co.absa.subatomic.infrastructure.tenant.view.jpa;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.tenant.TenantCreated;

@Component
public class TenantHandler {

    private TenantRepository tenantRepository;

    public TenantHandler(TenantRepository tenantRepository) {
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

}
