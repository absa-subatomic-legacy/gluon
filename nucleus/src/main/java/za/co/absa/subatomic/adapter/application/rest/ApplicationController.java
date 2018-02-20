package za.co.absa.subatomic.adapter.application.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import za.co.absa.subatomic.application.application.ApplicationService;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationEntity;
import za.co.absa.subatomic.infrastructure.application.view.jpa.BitbucketRepositoryEmbedded;

import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping("/applications")
@ExposesResourceFor(ApplicationResource.class)
public class ApplicationController {

    private ApplicationService applicationService;

    private ApplicationResourceAssembler assembler;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
        this.assembler = new ApplicationResourceAssembler();
    }

    @PostMapping
    ResponseEntity<ApplicationResource> create(
            @RequestBody ApplicationResource request) {
        String aggregateId = applicationService.newApplication(
                request.getName(),
                request.getDescription(),
                request.getProjectId(),
                request.getCreatedBy());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(aggregateId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    ResponseEntity<ApplicationResource> update(@PathVariable String id,
            @RequestBody ApplicationResource request) {
        if (request.getBitbucketRepository() != null) {
            applicationService.requestApplicationEnvironment(id,
                    request.getBitbucketRepository().getBitbucketId(),
                    request.getBitbucketRepository().getSlug(),
                    request.getBitbucketRepository().getName(),
                    request.getBitbucketRepository().getRepoUrl(),
                    request.getBitbucketRepository().getRemoteUrl(),
                    request.getProjectId(),
                    request.getCreatedBy());
        }

        return ResponseEntity.accepted()
                .body(assembler
                        .toResource(applicationService.findByApplictionId(id)));
    }

    @GetMapping("/{id}")
    ApplicationResource get(@PathVariable String id) {
        return assembler.toResource(applicationService.findByApplictionId(id));
    }

    @GetMapping
    Resources<ApplicationResource> list(
            @RequestParam(required = false) String name) {
        List<ApplicationResource> applications = new ArrayList<>();

        // TODO see if we can't use that functional library for Java that has pattern matching?
        if (StringUtils.isNotBlank(name)) {
            applications.add(
                    assembler.toResource(applicationService.findByName(name)));
        }

        if (StringUtils.isAllBlank(name)) {
            applications.addAll(applicationService.findAll().stream()
                    .map(assembler::toResource).collect(Collectors.toList()));
        }

        return new Resources<>(applications,
                linkTo(ApplicationController.class).withRel("self"),
                linkTo(methodOn(ApplicationController.class).list(name))
                        .withRel("self"));
    }

    private class ApplicationResourceAssembler
            extends
            ResourceAssemblerSupport<ApplicationEntity, ApplicationResource> {

        public ApplicationResourceAssembler() {
            super(ApplicationController.class, ApplicationResource.class);
        }

        @Override
        public ApplicationResource toResource(ApplicationEntity entity) {
            if (entity != null) {
                ApplicationResource resource = createResourceWithId(
                        entity.getApplicationId(),
                        entity);
                resource.setApplicationId(entity.getApplicationId());
                resource.setName(entity.getName());
                resource.setDescription(entity.getDescription());
                resource.setProjectId(entity.getProject().getProjectId());
                resource.setCreatedAt(entity.getCreatedAt());
                resource.setCreatedBy(entity.getCreatedBy().getMemberId());

                if (entity.getBitbucketRepository() != null) {
                    BitbucketRepositoryEmbedded bitbucketRepository = entity
                            .getBitbucketRepository();
                    resource.setBitbucketRepository(new BitbucketRepository(
                            bitbucketRepository.getBitbucketId(),
                            bitbucketRepository.getSlug(),
                            bitbucketRepository.getName(),
                            bitbucketRepository.getRepoUrl(),
                            bitbucketRepository.getRemoteUrl()));
                }

                return resource;
            }
            else {
                return null;
            }
        }
    }
}
