package za.co.absa.subatomic.domain.project;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class RequestBitbucketProject {

    @TargetAggregateIdentifier
    private String projectId;

    private BitbucketProject bitbucketProject;

    private TeamMemberId requestedBy;
}
