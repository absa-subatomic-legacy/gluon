package za.co.absa.subatomic.domain.project;

import lombok.Value;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;

import java.util.Set;

@Value
public class TeamsLinkedToProject {
    private ProjectId projectId;

    private TeamMemberId requestedBy;

    private Set<TeamId> teamsLinked;
}
