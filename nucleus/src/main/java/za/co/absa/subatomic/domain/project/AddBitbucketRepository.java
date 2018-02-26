package za.co.absa.subatomic.domain.project;

import java.util.Set;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class AddBitbucketRepository {

    @TargetAggregateIdentifier
    private String projectId;

    private TeamMemberId actionedBy;

    private BitbucketProject bitbucketProject;

    private Set<String> allAssociateProjectOwnerAndMemberIds;
}
