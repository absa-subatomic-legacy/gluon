package za.co.absa.subatomic.domain.project;

import lombok.Value;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class ProjectEnvironmentRequested {

    private String projectId;

    private String messageId;

    private TeamMemberId requestedBy;
}
