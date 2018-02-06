package za.co.absa.subatomic.adapter.team.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import za.co.absa.subatomic.adapter.member.rest.TeamMemberController;
import za.co.absa.subatomic.application.team.TeamService;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
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

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping("/teams")
@ExposesResourceFor(TeamResource.class)
public class TeamController {

    private final TeamResourceAssembler assembler;

    private TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
        this.assembler = new TeamResourceAssembler();
    }

    @PostMapping
    ResponseEntity<TeamResource> create(@RequestBody TeamResource request) {
        String aggregateId;
        if (request.getSlack() != null) {
            aggregateId = teamService.newTeamFromSlack(request.getName(),
                    request.getDescription(), request.getCreatedBy(),
                    request.getSlack().getTeamChannel());
        }
        else {
            aggregateId = teamService.newTeam(request.getName(),
                    request.getDescription(), request.getCreatedBy());
        }

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(aggregateId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    ResponseEntity<TeamResource> update(@PathVariable String id,
            @RequestBody TeamResource request) {
        if (!request.getOwners().isEmpty() || !request.getMembers().isEmpty()) {
            teamService.addTeamMembers(id,
                    request.getOwners().stream()
                            .map(TeamMemberIdResource::getMemberId)
                            .collect(toList()),
                    request.getMembers().stream()
                            .map(TeamMemberIdResource::getMemberId)
                            .collect(toList()));
        }

        if (request.getSlack() != null) {
            teamService.addSlackIdentity(id,
                    request.getSlack().getTeamChannel());
        }

        if (request.getDevOpsEnvironment() != null) {
            teamService.newDevOpsEnvironment(id,
                    request.getDevOpsEnvironment().getRequestedBy());
        }

        return ResponseEntity.accepted()
                .body(assembler.toResource(teamService.findByTeamId(id)));
    }

    @GetMapping("/{id}")
    TeamResource get(@PathVariable String id) {
        return assembler.toResource(teamService.findByTeamId(id));
    }

    @GetMapping
    Resources<TeamResource> list(
            @RequestParam(required = false) String name) {
        List<TeamResource> teams = new ArrayList<>();

        // TODO see if we can't use that functional library for Java that has pattern matching?
        if (StringUtils.isNotBlank(name)) {
            teams.add(
                    assembler.toResource(teamService.findByName(name)));
        }

        if (StringUtils.isAllBlank(name)) {
            teams.addAll(teamService.findAll().stream()
                    .map(assembler::toResource).collect(Collectors.toList()));
        }

        return new Resources<>(teams,
                linkTo(TeamController.class).withRel("self"),
                linkTo(methodOn(TeamController.class).list(name))
                        .withRel("self"));
    }

    private class TeamResourceAssembler
            extends ResourceAssemblerSupport<TeamEntity, TeamResource> {

        public TeamResourceAssembler() {
            super(TeamController.class, TeamResource.class);
        }

        @Override
        public TeamResource toResource(TeamEntity entity) {
            if (entity != null) {
                TeamResource resource = createResourceWithId(entity.getTeamId(),
                        entity);
                resource.setTeamId(entity.getTeamId());
                resource.setName(entity.getName());
                resource.setDescription(entity.getDescription());
                resource.setCreatedAt(entity.getCreatedAt());
                resource.setCreatedBy(entity.getCreatedBy().getMemberId());

                resource.getOwners().addAll(new TeamMemberIdResourceAssembler()
                        .toResources(entity.getOwners()));
                resource.getMembers().addAll(new TeamMemberIdResourceAssembler()
                        .toResources(entity.getMembers()));

                ofNullable(entity.getSlackDetails())
                        .ifPresent(slackDetails -> resource
                                .setSlack(
                                        new Slack(slackDetails
                                                .getTeamChannel())));
                return resource;
            }
            else {
                return null;
            }
        }
    }

    private class TeamMemberIdResourceAssembler extends
            ResourceAssemblerSupport<TeamMemberEntity, TeamMemberIdResource> {

        public TeamMemberIdResourceAssembler() {
            super(TeamMemberController.class, TeamMemberIdResource.class);
        }

        @Override
        public TeamMemberIdResource toResource(TeamMemberEntity entity) {
            TeamMemberIdResource resource = createResourceWithId(
                    entity.getMemberId(),
                    entity);
            resource.setMemberId(entity.getMemberId());
            return resource;
        }
    }
}
