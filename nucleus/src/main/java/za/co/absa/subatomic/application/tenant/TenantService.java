package za.co.absa.subatomic.application.tenant;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.exception.DuplicateRequestException;
import za.co.absa.subatomic.domain.tenant.NewTenant;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantRepository;

@Service
public class TenantService {

    private CommandGateway commandGateway;

    private TenantRepository tenantRepository;

    public TenantService(CommandGateway commandGateway,
            TenantRepository tenantRepository) {
        this.commandGateway = commandGateway;
        this.tenantRepository = tenantRepository;
    }

    public String newTenant(String name, String description) {
        TenantEntity existingTenant = this.findByName(name);
        if (existingTenant != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested tenant name {0} is not available.",
                    name));
        }

        return commandGateway.sendAndWait(
                new NewTenant(
                        UUID.randomUUID().toString(),
                        name,
                        description),
                1000,
                TimeUnit.SECONDS);
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
