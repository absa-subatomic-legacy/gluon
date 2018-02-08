package za.co.absa.subatomic.adapter.membership_request.rest;

import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import za.co.absa.subatomic.adapter.team.rest.TeamResource;
import za.co.absa.subatomic.application.membership_request.MembershipRequestService;
import za.co.absa.subatomic.infrastructure.membership_request.view.jpa.MembershipRequestEntity;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping("/teams/{teamId}/membership-request")
@ExposesResourceFor(TeamResource.class)
public class MembershipRequestController {

    private final MembershipRequestResourceAssembler assembler;

    private MembershipRequestService membershipRequestService;

    public MembershipRequestController(MembershipRequestService membershipRequestService) {
        this.membershipRequestService = membershipRequestService;
        this.assembler = new MembershipRequestResourceAssembler();
    }

    @PostMapping
    ResponseEntity<TeamResource> create(@PathVariable String teamId, @RequestBody NewMembershipRequestResource request) {
        String aggregateId = membershipRequestService.newMembershipRequest(teamId, request.getRequestorMemberId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(aggregateId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    MembershipRequestResource get(@PathVariable String teamId, @PathVariable String id) {
        return assembler.toResource(membershipRequestService.findByIdAndTeamId(id, teamId));
    }

    @GetMapping
    Resources<MembershipRequestResource> list(@PathVariable String teamId) {
        List<MembershipRequestResource> teams = new ArrayList<>();
                teams.addAll(membershipRequestService.findByTeamId(teamId).stream()
                .map(assembler::toResource).collect(Collectors.toList()));

        return new Resources<>(teams,
                linkTo(MembershipRequestController.class).withRel("self"),
                linkTo(methodOn(MembershipRequestController.class).list(teamId))
                        .withRel("self"));
    }

    private class MembershipRequestResourceAssembler extends
            ResourceAssemblerSupport<MembershipRequestEntity, MembershipRequestResource> {

        public MembershipRequestResourceAssembler() {
            super(MembershipRequestController.class, MembershipRequestResource.class);
        }

        @Override
        public MembershipRequestResource toResource(MembershipRequestEntity entity) {
            MembershipRequestResource resource = createResourceWithId(
                    entity.getId(),
                    entity);
            resource.setRequestedBy(entity.getRequestedBy().getMemberId());
            resource.setApprovedBy(entity.getApprovedBy().getMemberId());
            resource.setRequestStatus(entity.getRequestStatus());
            resource.setTeamId(entity.getTeamId());
            return resource;
        }
    }
}
