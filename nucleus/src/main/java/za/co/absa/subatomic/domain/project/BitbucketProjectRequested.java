package za.co.absa.subatomic.domain.project;

import lombok.Value;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class BitbucketProjectRequested {

    private ProjectId projectId;

    private BitbucketProject bitbucketProject;

    private final TeamMemberId requestedBy;
}
