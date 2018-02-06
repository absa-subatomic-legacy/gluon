package za.co.absa.subatomic.domain.pkg;

import lombok.Value;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class NewPackage {

    private String packageId;

    private String packageType;

    private String name;

    private String description;

    private TeamMemberId createdBy;

    private ProjectId project;
}
