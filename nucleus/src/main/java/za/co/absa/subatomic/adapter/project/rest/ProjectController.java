package za.co.absa.subatomic.adapter.project.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import za.co.absa.subatomic.adapter.team.rest.TeamController;
import za.co.absa.subatomic.application.project.ProjectService;
import za.co.absa.subatomic.infrastructure.project.view.jpa.BitbucketProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

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
@RequestMapping("/projects")
@ExposesResourceFor(ProjectResource.class)
public class ProjectController {

    private final ProjectResourceAssembler assembler;

    private ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
        this.assembler = new ProjectResourceAssembler();
    }

    @PostMapping
    ResponseEntity<ProjectResource> create(
            @RequestBody ProjectResource request) {
        // TODO do better error checking on the initial team
        String aggregateId = projectService.newProject(request.getName(),
                request.getDescription(), request.getCreatedBy(),
                request.getTeams().get(0).getTeamId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(aggregateId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    ResponseEntity<ProjectResource> update(@PathVariable String id,
            @RequestBody ProjectResource request) {
        if (request.getBitbucketProject() != null) {
            if (StringUtils.isNotBlank(
                    request.getBitbucketProject().getBitbucketProjectId())) {
                projectService.confirmBitbucketProjectCreated(id,
                        request.getBitbucketProject().getBitbucketProjectId(),
                        request.getBitbucketProject().getUrl());
            }
            else if (StringUtils
                    .isNotBlank(request.getBitbucketProject().getName())) {
                projectService.requestBitbucketProject(id,
                        request.getBitbucketProject().getName(),
                        request.getBitbucketProject().getDescription(),
                        request.getCreatedBy());
            }
        }

        if (request.getProjectEnvironment() != null) {
            projectService.newProjectEnvironment(id,
                    request.getProjectEnvironment().getRequestedBy());
        }

        return ResponseEntity.accepted()
                .body(assembler.toResource(projectService.findByProjectId(id)));
    }

    @GetMapping("/{id}")
    ProjectResource get(@PathVariable String id) {
        return assembler.toResource(projectService.findByProjectId(id));
    }

    @GetMapping
    Resources<ProjectResource> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String teamName) {
        List<ProjectResource> projects = new ArrayList<>();

        // TODO see if we can't use http://www.vavr.io/ for pattern matching?
        if (StringUtils.isNotBlank(name)) {
            projects.add(
                    assembler.toResource(projectService.findByName(name)));
        }

        if (StringUtils.isNotBlank(teamName)) {
            projects.addAll(
                    assembler.toResources(projectService.findByTeamName(name)));
        }

        if (StringUtils.isAllBlank(name)) {
            projects.addAll(projectService.findAll().stream()
                    .map(assembler::toResource).collect(Collectors.toList()));
        }

        return new Resources<>(projects,
                linkTo(TeamController.class).withRel("self"),
                linkTo(methodOn(ProjectController.class).list(name, teamName))
                        .withRel("self"));
    }

    private class ProjectResourceAssembler
            extends ResourceAssemblerSupport<ProjectEntity, ProjectResource> {

        public ProjectResourceAssembler() {
            super(ProjectController.class, ProjectResource.class);
        }

        @Override
        public ProjectResource toResource(ProjectEntity entity) {
            if (entity != null) {
                ProjectResource resource = createResourceWithId(
                        entity.getProjectId(),
                        entity);
                resource.setProjectId(entity.getProjectId());
                resource.setName(entity.getName());
                resource.setDescription(entity.getDescription());
                resource.setCreatedAt(entity.getCreatedAt());
                resource.setCreatedBy(entity.getCreatedBy().getMemberId());
                resource.setOwningTeam(new TeamResourceAssembler()
                        .toResource(entity.getOwningTeam()));
                resource.setTeams(
                        new TeamResourceAssembler()
                                .toResources(entity.getTeams()));

                if (entity.getBitbucketProject() != null) {
                    BitbucketProjectEntity bitbucketProject = entity
                            .getBitbucketProject();
                    resource.setBitbucketProject(BitbucketProjectResource
                            .builder()
                            .bitbucketProjectId(
                                    bitbucketProject.getBitbucketProjectId())
                            .key(bitbucketProject.getKey())
                            .name(bitbucketProject.getName())
                            .description(bitbucketProject.getDescription())
                            .url(bitbucketProject.getUrl())
                            .build());
                }

                return resource;
            }
            else {
                return null;
            }
        }
    }

    private class TeamResourceAssembler extends
            ResourceAssemblerSupport<TeamEntity, TeamResource> {

        public TeamResourceAssembler() {
            super(TeamController.class, TeamResource.class);
        }

        @Override
        public TeamResource toResource(TeamEntity entity) {
            TeamResource resource = createResourceWithId(
                    entity.getTeamId(),
                    entity);
            resource.setTeamId(entity.getTeamId());
            resource.setName(entity.getName());
            return resource;
        }
    }
    //
    // private class BitbucketProjectAssembler extends
    // ResourceAssemblerSupport<BitbucketProjectEntity, BitbucketProjectResource> {
    //
    // public BitbucketProjectAssembler() {
    // super(ProjectController.class, BitbucketProjectResource.class);
    // }
    //
    // @Override
    // public BitbucketProjectResource toResource(
    // BitbucketProjectEntity entity) {
    // BitbucketProjectResource resource = createResourceWithId(
    // entity.getBitbucketProjectId(),
    // entity);
    // resource.setKey(entity.getKey());
    // resource.setBitbucketProjectId(entity.getBitbucketProjectId());
    // resource.setName(entity.getName());
    // resource.setDescription(entity.getDescription());
    // resource.setUrl(entity.getUrl());
    // return resource;
    //
    // }
    // }
}
