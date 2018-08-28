package za.co.absa.subatomic.adapter.openshift.rest;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

@EqualsAndHashCode(callSuper = true)
@Data
public class OpenShiftResource extends ResourceSupport {
    String openshiftResourceId;

    String kind;

    String name;

    String resourceDetails;
}
