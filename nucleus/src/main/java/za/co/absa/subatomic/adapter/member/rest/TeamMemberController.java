package za.co.absa.subatomic.adapter.member.rest;

import static java.util.Optional.ofNullable;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import za.co.absa.subatomic.adapter.team.rest.TeamController;
import za.co.absa.subatomic.application.member.TeamMemberService;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@RestController
@RequestMapping("/members")
@ExposesResourceFor(TeamMemberResource.class)
public class TeamMemberController {

    private static final Logger log = LoggerFactory
            .getLogger(TeamMemberController.class);

    private final TeamMemberController.TeamMemberResourceAssembler assembler;

    private TeamMemberService teamMemberService;

    public TeamMemberController(TeamMemberService teamMemberService) {
        this.teamMemberService = teamMemberService;
        this.assembler = new TeamMemberController.TeamMemberResourceAssembler();
    }

    @PostMapping
    public ResponseEntity<TeamMemberResource> onboard(
            @RequestBody TeamMemberResource request) {
        TeamMemberEntity newTeamMember = teamMemberService.newTeamMember(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newTeamMember.getMemberId())
                .toUri();

        return ResponseEntity.created(location).body(this.assembler.toResource(newTeamMember));
    }

    @PutMapping("/{id}")
    ResponseEntity<TeamMemberResource> update(@PathVariable String id,
                                              @RequestBody TeamMemberResource request) {
        log.debug("Updating Team Member: {} -> {}", id, request);

        ofNullable(request.getSlack()).ifPresent(slack -> teamMemberService
                .addSlackDetails(id, slack.getScreenName(), slack.getUserId()));

        return ResponseEntity.accepted()
                .body(assembler
                        .toResource(teamMemberService
                                .getTeamMemberPersistenceHandler()
                                .findByTeamMemberId(id)));
    }

    @GetMapping("/{id}")
    TeamMemberResource get(@PathVariable String id) {
        return assembler.toResource(teamMemberService
                .getTeamMemberPersistenceHandler().findByTeamMemberId(id));
    }

    @GetMapping
    Resources<TeamMemberResource> list(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String slackScreenName,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String domainUsername) {
        List<TeamMemberResource> members = new ArrayList<>();

        // TODO see if we can't use that functional library for Java that has pattern matching?
        if (StringUtils.isNotBlank(email)) {
            members.add(
                    assembler.toResource(
                            teamMemberService.getTeamMemberPersistenceHandler()
                                    .findByEmail(email)));
        } else if (StringUtils.isNotBlank(slackScreenName)) {
            members.add(
                    assembler.toResource(
                            teamMemberService.getTeamMemberPersistenceHandler()
                                    .findBySlackScreenName(slackScreenName)));
        } else if (StringUtils.isNotBlank(teamId)) {
            members.addAll(
                    teamMemberService.getTeamMemberPersistenceHandler()
                            .findMembersAssociatedToTeam(teamId)
                            .stream()
                            .map(assembler::toResource)
                            .collect(Collectors.toList()));
        } else if (StringUtils.isNotBlank(domainUsername)) {
            members.add(assembler.toResource(
                    teamMemberService.getTeamMemberPersistenceHandler()
                            .findByDomainUsernameWithOrWithoutDomain(
                                    domainUsername)));
        } else if (StringUtils.isAllBlank(email, slackScreenName)) {
            members.addAll(teamMemberService.getTeamMemberPersistenceHandler()
                    .findAll().stream()
                    .map(assembler::toResource).collect(Collectors.toList()));
        }

        return new Resources<>(members,
                linkTo(TeamMemberController.class).withRel("self"),
                linkTo(methodOn(TeamMemberController.class).list(email,
                        slackScreenName, teamId, domainUsername))
                        .withRel("self"));
    }

    private class TeamMemberResourceAssembler extends
            ResourceAssemblerSupport<TeamMemberEntity, TeamMemberResource> {

        public TeamMemberResourceAssembler() {
            super(TeamMemberController.class, TeamMemberResource.class);
        }

        @Override
        public TeamMemberResource toResource(TeamMemberEntity entity) {
            if (entity != null) {
                TeamMemberResource resource = createResourceWithId(
                        entity.getMemberId(), entity);
                resource.setMemberId(entity.getMemberId());
                resource.setFirstName(entity.getFirstName());
                resource.setLastName(entity.getLastName());
                resource.setEmail(entity.getEmail());
                resource.setDomainUsername(entity.getDomainUsername());
                resource.setJoinedAt(entity.getJoinedAt());

                ofNullable(entity.getSlackDetails())
                        .ifPresent(slackDetails -> resource
                                .setSlack(
                                        new Slack(slackDetails.getScreenName(),
                                                slackDetails.getUserId())));

                resource.getTeams().addAll(new TeamResourceAssembler()
                        .toResources(entity.getTeams()));

                return resource;
            }

            return null;
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
            resource.setName(entity.getName());
            resource.setSlack((entity.getSlackDetails() != null)
                    ? new za.co.absa.subatomic.adapter.team.rest.Slack(
                    entity.getSlackDetails().getTeamChannel())
                    : null);
            return resource;
        }
    }
}
