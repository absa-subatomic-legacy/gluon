package za.co.absa.subatomic.adapter.project.rest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import za.co.absa.subatomic.adapter.team.rest.TeamController;
import za.co.absa.subatomic.adapter.team.rest.TeamResourceBaseAssembler;
import za.co.absa.subatomic.application.project.ProjectService;
import za.co.absa.subatomic.domain.project.DeploymentPipeline;
import za.co.absa.subatomic.infrastructure.project.view.jpa.BitbucketProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        String aggregateId = projectService.newProject(request);

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
            if (StringUtils.isNoneBlank(
                    request.getBitbucketProject().getBitbucketProjectId(),
                    request.getBitbucketProject().getName(),
                    request.getBitbucketProject().getKey(),
                    request.getBitbucketProject().getUrl())) {
                projectService.linkExistingBitbucketProject(id,
                        request.getBitbucketProject().getBitbucketProjectId(),
                        request.getBitbucketProject().getName(),
                        request.getBitbucketProject().getKey(),
                        request.getBitbucketProject().getDescription(),
                        request.getBitbucketProject().getUrl(),
                        request.getCreatedBy());
            }
        } else if (!request.getTeams().isEmpty()) {
            projectService.linkProjectToTeams(id, request.getCreatedBy(),
                    request.getTeams());
        } else if (request.getProjectEnvironment() != null) {
            projectService.newProjectEnvironment(id,
                    request.getProjectEnvironment().getRequestedBy());
        }
        if (request.getDevDeploymentPipeline() != null) {
            projectService.updateDevDeploymentPipeline(id,
                    request.getCreatedBy(), request.getDevDeploymentPipeline());
        }
        if (request.getReleaseDeploymentPipelines() != null) {
            projectService.updateReleaseDeploymentPipelines(id,
                    request.getCreatedBy(),
                    request.getReleaseDeploymentPipelines());
        }
        // If either the dev pipelines or release pipelines are changed, we should request
        // environment creation.
        if (request.getDevDeploymentPipeline() != null
                || request.getReleaseDeploymentPipelines() != null) {
            projectService.newProjectEnvironment(id, request.getCreatedBy());
        }

        return ResponseEntity.accepted()
                .body(assembler.toResource(projectService
                        .getProjectPersistenceHandler().findByProjectId(id)));
    }

    @PatchMapping("/{id}")
    ResponseEntity<ProjectResource> patch(@PathVariable String id,
                                          @RequestBody ProjectResource request) {
        if (request.getAdditionalEnvironments() != null) {
            projectService.addAdditionalEnvironments(
                    id,
                    request.getAdditionalEnvironments().stream().map(
                            AdditionalEnvironmentResource::getDisplayName
                    ).collect(Collectors.toList()),
                    request.getCreatedBy());
        }
        if (request.getAdditionalEnvironments() != null) {
            projectService.newProjectEnvironment(id, request.getCreatedBy());
        }

        return ResponseEntity.accepted()
                .body(assembler.toResource(projectService
                        .getProjectPersistenceHandler().findByProjectId(id)));
    }

    @GetMapping("/{id}")
    ProjectResource get(@PathVariable String id) {
        return assembler.toResource(projectService
                .getProjectPersistenceHandler().findByProjectId(id));
    }

    @GetMapping
    Resources<ProjectResource> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String teamName) {
        List<ProjectResource> projects = new ArrayList<>();

        // TODO see if we can't use http://www.vavr.io/ for pattern matching?
        if (StringUtils.isNotBlank(name)) {
            projects.add(
                    assembler.toResource(projectService
                            .getProjectPersistenceHandler().findByName(name)));
        }

        if (StringUtils.isNotBlank(teamName)) {
            projects.addAll(
                    assembler.toResources(
                            projectService.getProjectPersistenceHandler()
                                    .findByTeamName(teamName)));
        }

        if (StringUtils.isAllBlank(name, teamName)) {
            projects.addAll(projectService.getProjectPersistenceHandler()
                    .findAllProjects().stream()
                    .map(assembler::toResource).collect(Collectors.toList()));
        }

        return new Resources<>(projects,
                linkTo(TeamController.class).withRel("self"),
                linkTo(methodOn(ProjectController.class).list(name, teamName))
                        .withRel("self"));
    }

    @DeleteMapping("/{id}")
    ResponseEntity delete(@PathVariable String id) {
        projectService.deleteProject(id);
        return ResponseEntity.accepted().build();
    }

    private class ProjectResourceAssembler
            extends ResourceAssemblerSupport<ProjectEntity, ProjectResource> {

        public ProjectResourceAssembler() {
            super(ProjectController.class, ProjectResource.class);
        }

        @Override
        public ProjectResource toResource(ProjectEntity entity) {
            if (entity != null) {

                TeamResourceBaseAssembler teamResourceBaseAssembler = new TeamResourceBaseAssembler();

                ProjectResource resource = createResourceWithId(
                        entity.getProjectId(),
                        entity);
                resource.setProjectId(entity.getProjectId());
                resource.setName(entity.getName());
                resource.setDescription(entity.getDescription());
                resource.setCreatedAt(entity.getCreatedAt());
                resource.setCreatedBy(entity.getCreatedBy().getMemberId());
                resource.setOwningTeam(teamResourceBaseAssembler
                        .toResource(entity.getOwningTeam()));
                resource.setOwningTenant(
                        entity.getOwningTenant().getTenantId());
                resource.setTeams(
                        teamResourceBaseAssembler
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

                DeploymentPipelineResourceAssembler deploymentPipelineResourceAssembler = new DeploymentPipelineResourceAssembler();

                if (entity.getDevDeploymentPipeline() != null) {
                    resource.setDevDeploymentPipeline(
                            deploymentPipelineResourceAssembler.toResource(
                                    entity.getDevDeploymentPipeline()));
                }
                resource.setReleaseDeploymentPipelines(new ArrayList<>());
                if (entity.getReleaseDeploymentPipelines() != null) {
                    for (DeploymentPipeline releasePipeline : entity
                            .getReleaseDeploymentPipelines()) {
                        resource.getReleaseDeploymentPipelines().add(
                                deploymentPipelineResourceAssembler.toResource(
                                        releasePipeline));
                    }
                }

                resource.setAdditionalEnvironments(new ArrayList<>());
                if (entity.getAdditionalEnvironmentEntities() != null) {
                    resource.setAdditionalEnvironments(entity.getAdditionalEnvironmentEntities().stream().map(
                            additionalEnvironmentEntity -> AdditionalEnvironmentResource.builder().displayName(additionalEnvironmentEntity.getDisplayName()).build()
                    ).collect(Collectors.toList()));
                }

                return resource;
            } else {
                return null;
            }
        }
    }
}
