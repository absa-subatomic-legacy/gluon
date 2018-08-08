package za.co.absa.subatomic.adapter.pkg.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import za.co.absa.subatomic.adapter.project.rest.ProjectController;
import za.co.absa.subatomic.adapter.team.rest.TeamController;
import za.co.absa.subatomic.application.pkg.PackageService;
import za.co.absa.subatomic.infrastructure.pkg.view.jpa.PackageEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;

import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping("/packages")
@ExposesResourceFor(PackageResource.class)
public class PackageController {

    private final PackageResourceAssembler assembler;

    private PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
        this.assembler = new PackageResourceAssembler();
    }

    @PostMapping
    ResponseEntity<PackageResource> create(
            @RequestBody PackageResource request) {
        String aggregateId = packageService.newApplication(
                request.getPackageType(),
                request.getName(),
                request.getDescription(), request.getCreatedBy(),
                request.getProjectResource().getProjectId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(aggregateId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    PackageResource get(@PathVariable String id) {
        return assembler.toResource(packageService.findByApplicationId(id));
    }

    @GetMapping
    Resources<PackageResource> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String projectId) {
        List<PackageResource> packages = new ArrayList<>();

        // TODO see if we can't use that functional library for Java that has pattern matching?
        if (StringUtils.isNotBlank(projectId)) {
            packages.addAll(
                    assembler.toResources(
                            packageService.findByProjectId(projectId)));
        }
        else if (StringUtils.isNoneBlank(name, projectName)) {
            packages.add(
                    assembler.toResource(packageService
                            .findByNameAndProjectName(name, projectName)));
        }

        if (StringUtils.isAllBlank(name, projectName, projectId)) {
            packages.addAll(packageService.findAll().stream()
                    .map(assembler::toResource).collect(Collectors.toList()));
        }

        return new Resources<>(packages,
                linkTo(TeamController.class).withRel("self"),
                linkTo(methodOn(PackageController.class).list(name, projectName,
                        projectId))
                                .withRel("self"));
    }

    private class PackageResourceAssembler
            extends
            ResourceAssemblerSupport<PackageEntity, PackageResource> {

        public PackageResourceAssembler() {
            super(PackageController.class, PackageResource.class);
        }

        @Override
        public PackageResource toResource(PackageEntity entity) {
            if (entity != null) {
                PackageResource resource = createResourceWithId(
                        entity.getApplicationId(),
                        entity);
                resource.setApplicationId(entity.getApplicationId());
                resource.setName(entity.getName());
                resource.setDescription(entity.getDescription());
                resource.setCreatedAt(entity.getCreatedAt());
                resource.setCreatedBy(entity.getCreatedBy().getMemberId());

                resource.setProjectResource(
                        new ProjectResourceAssembler()
                                .toResource(entity.getProject()));

                return resource;
            }
            else {
                return null;
            }
        }
    }

    private class ProjectResourceAssembler extends
            ResourceAssemblerSupport<ProjectEntity, ProjectResource> {

        public ProjectResourceAssembler() {
            super(ProjectController.class, ProjectResource.class);
        }

        @Override
        public ProjectResource toResource(ProjectEntity entity) {
            ProjectResource resource = createResourceWithId(
                    entity.getProjectId(),
                    entity);
            resource.setProjectId(entity.getProjectId());
            resource.setName(entity.getName());
            return resource;
        }
    }
}
