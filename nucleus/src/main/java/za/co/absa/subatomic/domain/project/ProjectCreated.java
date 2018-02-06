package za.co.absa.subatomic.domain.project;

import lombok.Builder;
import lombok.Value;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
@Builder
public class ProjectCreated {

    private String projectId;

    private String name;

    private String description;

    private TeamMemberId createdBy;

    private TeamId team;
}
