package za.co.absa.subatomic.domain.application;

import lombok.Value;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class ApplicationEnvironmentRequested {

    private ApplicationId applicationId;

    private String name;

    private BitbucketGitRepository bitbucketGitRepository;

    private ProjectId projectId;

    private TeamMemberId requestedBy;
}
