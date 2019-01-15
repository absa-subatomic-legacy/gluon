package za.co.absa.subatomic.adapter.prod.application.rest;

import java.util.Date;
import java.util.List;

import org.springframework.hateoas.ResourceSupport;

import lombok.Data;
import lombok.EqualsAndHashCode;
import za.co.absa.subatomic.adapter.openshift.rest.OpenShiftResource;
import za.co.absa.subatomic.adapter.project.rest.DeploymentPipelineResource;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApplicationProdRequestResource extends ResourceSupport {

    private String applicationProdRequestId;

    private String applicationId;

    private DeploymentPipelineResource deploymentPipeline;

    private Date createdAt;

    private String actionedBy;

    private List<OpenShiftResource> openShiftResources;
}
