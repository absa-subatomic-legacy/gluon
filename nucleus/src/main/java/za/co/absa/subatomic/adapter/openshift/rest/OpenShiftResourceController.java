package za.co.absa.subatomic.adapter.openshift.rest;

import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.co.absa.subatomic.adapter.prod.application.rest.ApplicationProdRequestResource;
import za.co.absa.subatomic.application.openshift.OpenShiftResourceService;
import za.co.absa.subatomic.infrastructure.openshift.view.jpa.OpenShiftResourceEntity;

@RestController
@RequestMapping("/openShiftResources")
@ExposesResourceFor(ApplicationProdRequestResource.class)
public class OpenShiftResourceController {

    private OpenShiftResourceService openShiftResourceService;

    public OpenShiftResourceController(
            OpenShiftResourceService openShiftResourceService) {
        this.openShiftResourceService = openShiftResourceService;
    }

    @GetMapping("/{id}")
    OpenShiftResourceEntity get(@PathVariable String id) {
        return openShiftResourceService.findByOpenshiftResourceId(id);
    }

}
