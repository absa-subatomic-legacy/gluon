package za.co.absa.subatomic.infrastructure.atomist.resource;

import lombok.Builder;
import lombok.Value;
import za.co.absa.subatomic.adapter.application.rest.BitbucketRepository;
import za.co.absa.subatomic.domain.application.ApplicationType;
import za.co.absa.subatomic.domain.project.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
@Builder
public class AtomistApplication {

    private String applicationId;

    private String name;

    private String description;

    private ApplicationType applicationType;

    private ProjectId projectId;

    private TeamMemberId createdBy;

    private Boolean requestConfiguration;

    private BitbucketRepository bitbucketRepository;
}
