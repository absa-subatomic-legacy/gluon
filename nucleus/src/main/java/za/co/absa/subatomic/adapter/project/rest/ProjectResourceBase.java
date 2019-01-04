package za.co.absa.subatomic.adapter.project.rest;

import org.springframework.hateoas.ResourceSupport;

import lombok.Data;
import lombok.EqualsAndHashCode;
import za.co.absa.subatomic.domain.project.ProjectBase;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProjectResourceBase extends ResourceSupport
        implements ProjectBase {

    protected String projectId;

    protected String name;

    protected String description;

    protected String owningTenant;
}
