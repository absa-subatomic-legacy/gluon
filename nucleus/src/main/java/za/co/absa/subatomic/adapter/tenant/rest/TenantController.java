package za.co.absa.subatomic.adapter.tenant.rest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import za.co.absa.subatomic.adapter.team.rest.TeamController;
import za.co.absa.subatomic.application.tenant.TenantService;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;

@RestController
@RequestMapping("/tenants")
@ExposesResourceFor(TenantResource.class)
public class TenantController {

    private final TenantResourceAssembler assembler;

    private TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
        this.assembler = new TenantResourceAssembler();
    }

    @PostMapping
    ResponseEntity<TenantResource> create(
            @RequestBody TenantResource request) {
        // TODO do better error checking on the initial team
        String aggregateId = tenantService.newTenant(request.getName(),
                request.getDescription());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(aggregateId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    TenantResource get(@PathVariable String id) {
        return assembler.toResource(tenantService.findByTenantId(id));
    }

    @GetMapping
    Resources<TenantResource> list() {
        List<TenantResource> projects = new ArrayList<>();

        projects.addAll(tenantService.findAll().stream()
                .map(assembler::toResource).collect(Collectors.toList()));

        return new Resources<>(projects,
                linkTo(TeamController.class).withRel("self"),
                linkTo(methodOn(TenantController.class).list())
                        .withRel("self"));
    }

    private class TenantResourceAssembler extends
            ResourceAssemblerSupport<TenantEntity, TenantResource> {

        public TenantResourceAssembler() {
            super(TenantController.class, TenantResource.class);
        }

        @Override
        public TenantResource toResource(TenantEntity entity) {
            TenantResource resource = createResourceWithId(
                    entity.getTenantId(),
                    entity);
            resource.setTenantId(entity.getTenantId());
            resource.setName(entity.getName());
            resource.setDescription(entity.getDescription());
            return resource;
        }
    }
}
