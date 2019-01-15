package za.co.absa.subatomic.adapter.prod.application.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import za.co.absa.subatomic.adapter.openshift.rest.OpenShiftResource;
import za.co.absa.subatomic.adapter.openshift.rest.OpenShiftResourceAssembler;
import za.co.absa.subatomic.adapter.project.rest.DeploymentPipelineResourceAssembler;
import za.co.absa.subatomic.application.prod.application.ApplicationProdRequestService;
import za.co.absa.subatomic.infrastructure.prod.application.view.jpa.ApplicationProdRequestEntity;

@RestController
@RequestMapping("/applicationProdRequests")
@ExposesResourceFor(ApplicationProdRequestResource.class)
public class ApplicationProdRequestController {

    private ApplicationProdRequestService applicationProdRequestService;

    private ApplicationProdRequestResourceAssembler assembler;

    public ApplicationProdRequestController(
            ApplicationProdRequestService applicationProdRequestService) {
        this.applicationProdRequestService = applicationProdRequestService;
        this.assembler = new ApplicationProdRequestResourceAssembler();
    }

    @PostMapping
    ResponseEntity<ApplicationProdRequestResource> create(
            @RequestBody ApplicationProdRequestResource request) {

        ApplicationProdRequestEntity applicationProdRequestEntity = this.applicationProdRequestService
                .newApplicationProdRequest(
                        request.getApplicationId(),
                        request.getActionedBy(),
                        request.getDeploymentPipeline().getPipelineId(),
                        request.getOpenShiftResources());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(applicationProdRequestEntity
                        .getApplicationProdRequestId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    ApplicationProdRequestResource get(@PathVariable String id) {
        return assembler.toResource(this.applicationProdRequestService
                .findApplicationProjectRequestById(id));
    }

    private class ApplicationProdRequestResourceAssembler extends
            ResourceAssemblerSupport<ApplicationProdRequestEntity, ApplicationProdRequestResource> {

        public ApplicationProdRequestResourceAssembler() {
            super(ApplicationProdRequestController.class,
                    ApplicationProdRequestResource.class);
        }

        @Override
        public ApplicationProdRequestResource toResource(
                ApplicationProdRequestEntity entity) {
            if (entity != null) {
                ApplicationProdRequestResource resource = createResourceWithId(
                        entity.getApplicationProdRequestId(), entity);
                resource.setApplicationProdRequestId(
                        entity.getApplicationProdRequestId());
                resource.setApplicationId(
                        entity.getApplication().getApplicationId());
                resource.setActionedBy(entity.getActionedBy().getMemberId());
                resource.setCreatedAt(entity.getCreatedAt());
                resource.setDeploymentPipeline(
                        new DeploymentPipelineResourceAssembler()
                                .toResource(entity.getDeploymentPipeline()));

                List<OpenShiftResource> openShiftResources = new ArrayList<>();
                OpenShiftResourceAssembler openShiftResourceAssembler = new OpenShiftResourceAssembler();
                openShiftResources
                        .addAll(entity.getOpenShiftResources().stream()
                                .map(openShiftResourceAssembler::toResource)
                                .collect(Collectors.toList()));

                resource.setOpenShiftResources(openShiftResources);

                return resource;
            }
            else {
                return null;
            }
        }
    }
}
