package za.co.absa.subatomic.adapter.prod.generic.rest;

import java.util.Date;
import java.util.List;

import org.springframework.hateoas.ResourceSupport;

import lombok.Data;
import lombok.EqualsAndHashCode;
import za.co.absa.subatomic.adapter.member.rest.TeamMemberResourceBase;
import za.co.absa.subatomic.adapter.openshift.rest.OpenShiftResource;
import za.co.absa.subatomic.adapter.project.rest.ProjectResourceBase;

@EqualsAndHashCode(callSuper = true)
@Data
public class GenericProdRequestResource extends ResourceSupport {

    private String genericProdRequestId;

    private ProjectResourceBase project;

    private Date createdAt;

    private TeamMemberResourceBase actionedBy;

    private List<OpenShiftResource> openShiftResources;
}
