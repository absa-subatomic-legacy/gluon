package za.co.absa.subatomic.adapter.prod.project.rest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

import za.co.absa.subatomic.adapter.application.rest.ApplicationController;
import za.co.absa.subatomic.adapter.member.rest.TeamMemberResourceBase;
import za.co.absa.subatomic.adapter.member.rest.TeamMemberResourceBaseAssembler;
import za.co.absa.subatomic.adapter.project.rest.DeploymentPipelineResourceAssembler;
import za.co.absa.subatomic.adapter.project.rest.ProjectResourceBaseAssembler;
import za.co.absa.subatomic.application.prod.project.ProjectProdRequestService;
import za.co.absa.subatomic.domain.exception.InvalidRequestException;
import za.co.absa.subatomic.domain.prod.project.ProjectProductionRequestStatus;
import za.co.absa.subatomic.infrastructure.prod.project.view.jpa.ProjectProdRequestEntity;

@RestController
@RequestMapping("/projectProdRequests")
@ExposesResourceFor(ProjectProdRequestResource.class)
public class ProjectProdRequestController {

    private ProjectProdRequestResourceAssembler assembler;

    private ProjectProdRequestService projectProdRequestService;

    public ProjectProdRequestController(
            ProjectProdRequestService projectProdRequestService) {
        this.projectProdRequestService = projectProdRequestService;
        this.assembler = new ProjectProdRequestResourceAssembler();
    }

    @PostMapping
    ResponseEntity<ProjectProdRequestResource> create(
            @RequestBody ProjectProdRequestResource request) {

        String projectId = tryGetProjectId(request);
        String memberId = tryGetActionByMemberId(request);
        String pipelineId = tryGetPipelineId(request);

        if (StringUtils.isAnyBlank(projectId, memberId, pipelineId)) {
            throw new InvalidRequestException(
                    "Failed to create prod request. Please make sure you have sent a correctly formatted request");
        }

        ProjectProdRequestEntity projectProdRequestEntity = this.projectProdRequestService
                .createProjectProdRequest(projectId,
                        memberId, pipelineId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(projectProdRequestEntity
                        .getProjectProdRequestId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    ResponseEntity<ProjectProdRequestResource> update(@PathVariable String id,
            @RequestBody ProjectProdRequestResource request) {

        String actionedById = this.tryGetActionByMemberId(request);

        if (StringUtils.isNotBlank(actionedById)
                && request.getApprovalStatus() != null
                && StringUtils.isNotBlank(id)) {
            if (request
                    .getApprovalStatus() == ProjectProductionRequestStatus.APPROVED) {
                this.projectProdRequestService
                        .addProjectProdRequestApproval(id, actionedById);
            }
            else if (request
                    .getApprovalStatus() == ProjectProductionRequestStatus.REJECTED) {
                this.projectProdRequestService
                        .rejectProjectProdRequest(id, actionedById);
            }
        }
        else {
            throw new InvalidRequestException(
                    "Project prod request update is malformed.");
        }

        return ResponseEntity.accepted()
                .body(assembler.toResource(projectProdRequestService
                        .findByProjectProdRequestId(id)));
    }

    @GetMapping("/{id}")
    ProjectProdRequestResource get(@PathVariable String id) {
        return assembler.toResource(this.projectProdRequestService
                .findByProjectProdRequestId(id));
    }

    @GetMapping
    Resources<ProjectProdRequestResource> list(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String pipelineId) {

        List<ProjectProdRequestResource> projectProdRequests = new ArrayList<>();
        if (StringUtils.isNotBlank(projectId)
                && StringUtils.isNotBlank(pipelineId)) {
            projectProdRequests.addAll(this.projectProdRequestService
                    .findByProjectIdAndPipelineId(projectId, pipelineId)
                    .stream()
                    .map(assembler::toResource).collect(Collectors.toList()));
        }
        else if (StringUtils.isNotBlank(projectId)) {
            projectProdRequests.addAll(this.projectProdRequestService
                    .findByProjectId(projectId)
                    .stream()
                    .map(assembler::toResource).collect(Collectors.toList()));
        }
        else {
            projectProdRequests.addAll(this.projectProdRequestService.findAll()
                    .stream()
                    .map(assembler::toResource).collect(Collectors.toList()));
        }

        return new Resources<>(projectProdRequests,
                linkTo(ApplicationController.class).withRel("self"),
                linkTo(methodOn(ProjectProdRequestController.class)
                        .list(projectId, pipelineId))
                                .withRel("self"));
    }

    private String tryGetActionByMemberId(
            ProjectProdRequestResource projectProdRequestResource) {
        if (projectProdRequestResource.getActionedBy() != null
                && StringUtils.isNotBlank(projectProdRequestResource
                        .getActionedBy().getMemberId())) {
            return projectProdRequestResource.getActionedBy().getMemberId();
        }
        return "";
    }

    private String tryGetProjectId(
            ProjectProdRequestResource projectProdRequestResource) {
        if (projectProdRequestResource.getProject() != null
                && StringUtils.isNotBlank(projectProdRequestResource
                        .getProject().getProjectId())) {
            return projectProdRequestResource.getProject().getProjectId();
        }
        return "";
    }

    private String tryGetPipelineId(
            ProjectProdRequestResource projectProdRequestResource) {
        if (projectProdRequestResource.getDeploymentPipeline() != null
                && StringUtils.isNotBlank(projectProdRequestResource
                        .getDeploymentPipeline().getPipelineId())) {
            return projectProdRequestResource.getDeploymentPipeline()
                    .getPipelineId();
        }
        return "";
    }

    private class ProjectProdRequestResourceAssembler extends
            ResourceAssemblerSupport<ProjectProdRequestEntity, ProjectProdRequestResource> {

        public ProjectProdRequestResourceAssembler() {
            super(ProjectProdRequestController.class,
                    ProjectProdRequestResource.class);
        }

        @Override
        public ProjectProdRequestResource toResource(
                ProjectProdRequestEntity entity) {
            if (entity != null) {
                ProjectProdRequestResource resource = createResourceWithId(
                        entity.getProjectProdRequestId(), entity);
                resource.setProjectProdRequestId(
                        entity.getProjectProdRequestId());
                resource.setCreatedAt(entity.getCreatedAt());
                resource.setClosedAt(entity.getClosedAt());
                resource.setDeploymentPipeline(
                        new DeploymentPipelineResourceAssembler()
                                .toResource(entity.getDeploymentPipeline()));

                ProjectResourceBaseAssembler projectResourceBaseAssembler = new ProjectResourceBaseAssembler();
                resource.setProject(projectResourceBaseAssembler.toResource(
                        entity.getProject()));

                TeamMemberResourceBaseAssembler teamMemberResourceBaseAssembler = new TeamMemberResourceBaseAssembler();
                List<TeamMemberResourceBase> authorizingTeamMembers = new ArrayList<>();
                authorizingTeamMembers.addAll(entity.getAuthorizingMembers()
                        .stream()
                        .map(teamMemberResourceBaseAssembler::toResource)
                        .collect(Collectors.toList()));

                resource.setAuthorizingMembers(authorizingTeamMembers);
                resource.setActionedBy(teamMemberResourceBaseAssembler
                        .toResource(entity.getActionedBy()));
                resource.setRejectingMember(teamMemberResourceBaseAssembler
                        .toResource(entity.getRejectingMember()));

                resource.setApprovalStatus(entity.getApprovalStatus());

                return resource;
            }
            else {
                return null;
            }
        }
    }
}
