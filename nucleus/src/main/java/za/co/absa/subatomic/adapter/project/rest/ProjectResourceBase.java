package za.co.absa.subatomic.adapter.project.rest;

import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

import lombok.Data;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProjectResourceBase extends ResourceSupport {

    protected String projectId;

    protected String name;

    protected String description;

    protected String owningTenant;
}
