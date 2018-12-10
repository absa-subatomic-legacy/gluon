package za.co.absa.subatomic.infrastructure.atomist.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import za.co.absa.subatomic.domain.project.TeamId;
import za.co.absa.subatomic.domain.project.TenantId;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Data
@Builder
@AllArgsConstructor
public class AtomistProject {

    private String projectId;

    private String name;

    private String description;

    private TeamMemberId createdBy;

    private TeamId team;

    private TenantId tenant;
}
