package za.co.absa.subatomic.infrastructure.atomist.resource.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import za.co.absa.subatomic.domain.team.TeamSlackIdentity;

@Data
@AllArgsConstructor
public class AtomistTeamBase {
    private String teamId;

    private String name;

    private String openShiftCloud;

    private TeamSlackIdentity slackIdentity;
}
