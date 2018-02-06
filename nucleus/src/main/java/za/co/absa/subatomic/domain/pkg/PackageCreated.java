package za.co.absa.subatomic.domain.pkg;

import lombok.Value;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class PackageCreated {

    private String packageId;

    private PackageType packageType;

    private String name;

    private String description;

    private TeamMemberId createdBy;

    private ProjectId project;
}
