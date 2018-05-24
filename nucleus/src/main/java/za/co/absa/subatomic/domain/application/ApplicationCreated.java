package za.co.absa.subatomic.domain.application;

import lombok.Builder;
import lombok.Value;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
@Builder
public class ApplicationCreated {

    private String applicationId;

    private String name;

    private String description;

    private ApplicationType applicationType;

    private ProjectId projectId;

    private TeamMemberId createdBy;

    private Boolean requestConfiguration;

    private BitbucketGitRepository bitbucketRepository;
}
