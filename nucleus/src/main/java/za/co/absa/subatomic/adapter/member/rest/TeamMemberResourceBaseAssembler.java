package za.co.absa.subatomic.adapter.member.rest;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;

import static java.util.Optional.ofNullable;

public class TeamMemberResourceBaseAssembler extends
        ResourceAssemblerSupport<TeamMemberEntity, TeamMemberResourceBase> {

    public TeamMemberResourceBaseAssembler() {
        super(TeamMemberController.class, TeamMemberResourceBase.class);
    }

    @Override
    public TeamMemberResourceBase toResource(TeamMemberEntity entity) {
        if (entity != null) {
            TeamMemberResourceBase resource = createResourceWithId(
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

            return resource;
        }

        return null;
    }
}