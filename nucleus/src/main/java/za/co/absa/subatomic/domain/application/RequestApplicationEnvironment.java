package za.co.absa.subatomic.domain.application;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class RequestApplicationEnvironment {

    @TargetAggregateIdentifier
    private String applicationId;

    private String name;

    private BitbucketGitRepository bitbucketRepository;

    private ProjectId projectId;

    private TeamMemberId requestedBy;
}
