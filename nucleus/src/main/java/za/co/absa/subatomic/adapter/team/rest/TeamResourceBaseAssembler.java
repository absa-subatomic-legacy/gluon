package za.co.absa.subatomic.adapter.team.rest;

import static java.util.Optional.ofNullable;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

public class TeamResourceBaseAssembler
        extends ResourceAssemblerSupport<TeamEntity, TeamResourceBase> {

    public TeamResourceBaseAssembler() {
        super(TeamController.class, TeamResourceBase.class);
    }

    @Override
    public TeamResourceBase toResource(TeamEntity entity) {
        if (entity != null) {
            TeamResourceBase resource = createResourceWithId(entity.getTeamId(),
                    entity);
            resource.setTeamId(entity.getTeamId());
            resource.setName(entity.getName());
            resource.setDescription(entity.getDescription());
            resource.setOpenShiftCloud(entity.getOpenShiftCloud());
            resource.setCreatedAt(entity.getCreatedAt());
            resource.setCreatedBy(entity.getCreatedBy().getMemberId());

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