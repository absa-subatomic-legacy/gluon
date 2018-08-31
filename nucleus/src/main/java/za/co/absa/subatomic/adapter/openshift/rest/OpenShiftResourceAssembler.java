package za.co.absa.subatomic.adapter.openshift.rest;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

import za.co.absa.subatomic.infrastructure.openshift.view.jpa.OpenShiftResourceEntity;

public class OpenShiftResourceAssembler extends
        ResourceAssemblerSupport<OpenShiftResourceEntity, OpenShiftResource> {

    public OpenShiftResourceAssembler() {
        super(OpenShiftResourceController.class,
                OpenShiftResource.class);
    }

    @Override
    public OpenShiftResource toResource(
            OpenShiftResourceEntity entity) {
        if (entity != null) {
            OpenShiftResource resource = createResourceWithId(
                    entity.getOpenShiftResourceId(), entity);

            resource.setOpenshiftResourceId(entity.getOpenShiftResourceId());
            resource.setKind(entity.getKind());
            resource.setName(entity.getName());
            resource.setResourceDetails(entity.getResourceDetails());

            return resource;
        }
        else {
            return null;
        }
    }
}