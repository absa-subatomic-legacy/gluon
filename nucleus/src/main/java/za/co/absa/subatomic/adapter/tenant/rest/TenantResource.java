package za.co.absa.subatomic.adapter.tenant.rest;

import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

import lombok.Data;

@EqualsAndHashCode(callSuper = true)
@Data
public class TenantResource extends ResourceSupport {

    private String tenantId;

    private String name;

    private String description;

}
