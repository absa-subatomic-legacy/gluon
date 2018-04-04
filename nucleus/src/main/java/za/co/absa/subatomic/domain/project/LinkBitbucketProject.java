package za.co.absa.subatomic.domain.project;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import za.co.absa.subatomic.domain.team.TeamMemberId;

import java.util.Set;

@Value
public class LinkBitbucketProject {

    @TargetAggregateIdentifier
    private String projectId;

    private BitbucketProject bitbucketProject;

    private TeamMemberId requestedBy;

    private Set<String> allAssociateProjectOwnerAndMemberIds;
}
