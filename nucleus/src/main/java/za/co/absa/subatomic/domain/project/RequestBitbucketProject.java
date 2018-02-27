package za.co.absa.subatomic.domain.project;

import java.util.Set;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class RequestBitbucketProject {

    @TargetAggregateIdentifier
    private String projectId;

    private BitbucketProject bitbucketProject;

    private TeamMemberId requestedBy;

    private Set<String> allAssociateProjectOwnerAndMemberIds;
}
