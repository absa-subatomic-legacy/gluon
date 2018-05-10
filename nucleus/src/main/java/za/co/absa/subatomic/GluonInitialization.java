package za.co.absa.subatomic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import za.co.absa.subatomic.application.tenant.TenantService;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;

@Component
public class GluonInitialization implements ApplicationRunner {

    private TenantService tenantService;

    @Autowired
    public GluonInitialization(TenantService tenantService) {

        this.tenantService = tenantService;
    }

    @Override
    public void run(ApplicationArguments applicationArguments)
            throws Exception {
        createDefaultTenant();
    }

    private void createDefaultTenant() {
        TenantEntity defaultTenant = this.tenantService.findByName("Default");
        if (defaultTenant == null) {
            this.tenantService.newTenant("Default",
                    "The default project tenant");
        }
    }
}
