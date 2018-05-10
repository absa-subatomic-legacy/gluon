package za.co.absa.subatomic.adapter.tenant.rest;

import org.springframework.hateoas.ResourceSupport;

import lombok.Data;

@Data
public class TenantResource extends ResourceSupport {

    private String tenantId;

    private String name;

    private String description;

}
