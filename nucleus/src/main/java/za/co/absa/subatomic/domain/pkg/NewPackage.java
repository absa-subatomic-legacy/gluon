package za.co.absa.subatomic.domain.pkg;

import lombok.Value;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

import java.util.Set;

@Value
public class NewPackage {

    private String packageId;

    private String packageType;

    private String name;

    private String description;

    private TeamMemberId createdBy;

    private ProjectId project;

    private Set<TeamEntity> projectAssociatedTeams;
}
