package za.co.absa.subatomic.infrastructure.atomist.resource;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import za.co.absa.subatomic.domain.team.TeamSlackIdentity;

@Getter
@Setter
public class AtomistTeam extends AtomistTeamBase {

    private List<AtomistMemberBase> owners = new ArrayList<>();

    private List<AtomistMemberBase> members = new ArrayList<>();

    public AtomistTeam(String teamId, String name, String openShiftCloud,
            TeamSlackIdentity slackIdentity) {
        super(teamId, name, openShiftCloud, slackIdentity);
    }

}
