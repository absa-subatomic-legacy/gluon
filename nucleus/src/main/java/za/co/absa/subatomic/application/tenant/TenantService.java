package za.co.absa.subatomic.application.tenant;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.exception.DuplicateRequestException;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantPersistenceHandler;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantRepository;

@Service
public class TenantService {

    private TenantPersistenceHandler tenantPersistenceHandler;

    private TenantRepository tenantRepository;

    public TenantService(TenantPersistenceHandler tenantPersistenceHandler,
            TenantRepository tenantRepository) {
        this.tenantPersistenceHandler = tenantPersistenceHandler;
        this.tenantRepository = tenantRepository;
    }

    public TenantEntity newTenant(String name, String description) {
        TenantEntity existingTenant = this.findByName(name);
        if (existingTenant != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested tenant name {0} is not available.",
                    name));
        }

        return this.tenantPersistenceHandler.createTenant(name,
                description);
    }

    @Transactional(readOnly = true)
    public TenantEntity findByName(String name) {
        return tenantRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public TenantEntity findByTenantId(String id) {
        return tenantRepository.findByTenantId(id);
    }

    @Transactional(readOnly = true)
    public List<TenantEntity> findAll() {
        return tenantRepository.findAll();
    }

}
