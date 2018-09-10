package za.co.absa.subatomic.adapter.prod.generic.rest;

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

import za.co.absa.subatomic.adapter.member.rest.TeamMemberResourceBaseAssembler;
import za.co.absa.subatomic.adapter.openshift.rest.OpenShiftResource;
import za.co.absa.subatomic.adapter.openshift.rest.OpenShiftResourceAssembler;
import za.co.absa.subatomic.adapter.project.rest.ProjectResourceBaseAssembler;
import za.co.absa.subatomic.application.prod.generic.GenericProdRequestService;
import za.co.absa.subatomic.infrastructure.prod.generic.view.jpa.GenericProdRequestEntity;

@RestController
@RequestMapping("/genericProdRequests")
@ExposesResourceFor(GenericProdRequestResource.class)
public class GenericProdRequestController {

    private GenericProdRequestResourceAssembler assembler;

    private GenericProdRequestService genericProdRequestService;

    public GenericProdRequestController(
            GenericProdRequestService genericProdRequestService) {
        this.genericProdRequestService = genericProdRequestService;
        this.assembler = new GenericProdRequestResourceAssembler();
    }

    @PostMapping
    ResponseEntity<GenericProdRequestResource> create(
            @RequestBody GenericProdRequestResource request) {

        GenericProdRequestEntity genericProdRequestEntity = this.genericProdRequestService
                .newGenericProdRequest(
                        request.getProject().getProjectId(),
                        request.getActionedBy().getMemberId(),
                        request.getOpenShiftResources());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(genericProdRequestEntity
                        .getGenericProdRequestId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    GenericProdRequestResource get(@PathVariable String id) {
        return assembler.toResource(this.genericProdRequestService
                .findGenericProdRequestById(id));
    }

    private class GenericProdRequestResourceAssembler extends
            ResourceAssemblerSupport<GenericProdRequestEntity, GenericProdRequestResource> {

        public GenericProdRequestResourceAssembler() {
            super(GenericProdRequestController.class,
                    GenericProdRequestResource.class);
        }

        @Override
        public GenericProdRequestResource toResource(
                GenericProdRequestEntity entity) {
            if (entity != null) {
                GenericProdRequestResource resource = createResourceWithId(
                        entity.getGenericProdRequestId(), entity);
                resource.setGenericProdRequestId(
                        entity.getGenericProdRequestId());

                ProjectResourceBaseAssembler projectResourceBaseAssembler = new ProjectResourceBaseAssembler();
                resource.setProject(
                        projectResourceBaseAssembler
                                .toResource(entity.getProject()));

                TeamMemberResourceBaseAssembler teamMemberResourceBaseAssembler = new TeamMemberResourceBaseAssembler();
                resource.setActionedBy(teamMemberResourceBaseAssembler
                        .toResource(entity.getActionedBy()));
                resource.setCreatedAt(entity.getCreatedAt());

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
