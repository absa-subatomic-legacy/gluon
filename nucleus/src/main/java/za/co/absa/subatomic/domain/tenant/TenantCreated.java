package za.co.absa.subatomic.domain.tenant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TenantCreated {

    private String tenantId;

    private String name;

    private String description;
}
