package za.co.absa.subatomic.domain.application;

import java.util.Set;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class NewApplication {

    @TargetAggregateIdentifier
    private String applicationId;

    private String name;

    private String description;

    private ApplicationType applicationType;

    private ProjectId projectId;

    private TeamMemberId requestedBy;

    private Boolean requestConfiguration;

    private BitbucketGitRepository bitbucketRepository;

    private Set<String> allAssociateProjectOwnerAndMemberIds;
}
