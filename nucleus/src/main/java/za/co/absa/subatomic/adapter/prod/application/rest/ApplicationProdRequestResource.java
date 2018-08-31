package za.co.absa.subatomic.adapter.prod.application.rest;

import java.util.Date;
import java.util.List;

import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

import lombok.Data;
import za.co.absa.subatomic.adapter.openshift.rest.OpenShiftResource;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApplicationProdRequestResource extends ResourceSupport {

    private String applicationProdRequestId;

    private String applicationId;

    private Date createdAt;

    private String actionedBy;

    private List<OpenShiftResource> openShiftResources;
}
