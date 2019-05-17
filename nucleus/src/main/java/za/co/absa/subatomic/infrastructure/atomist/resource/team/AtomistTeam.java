package za.co.absa.subatomic.infrastructure.atomist.resource.team;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import za.co.absa.subatomic.domain.team.TeamSlackIdentity;
import za.co.absa.subatomic.infrastructure.atomist.resource.AtomistMemberBase;

@Getter
@Setter
public class AtomistTeam extends AtomistTeamBase {

    public static class Builder {

        private String teamId;

        private String name;

        private String openShiftCloud;

        private TeamSlackIdentity slackIdentity;

        private List<AtomistMemberBase> owners = new ArrayList<>();

        private List<AtomistMemberBase> members = new ArrayList<>();

        public AtomistTeam build() {
            AtomistTeam team = new AtomistTeam(this.teamId, this.name,
                    this.openShiftCloud,
                    this.slackIdentity);
            team.setMembers(this.members);
            team.setOwners(this.owners);
            return team;
        }

        public Builder teamId(final String teamId) {
            this.teamId = teamId;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder openShiftCloud(final String openShiftCloud) {
            this.openShiftCloud = openShiftCloud;
            return this;
        }

        public Builder slackIdentity(final TeamSlackIdentity slackIdentity) {
            this.slackIdentity = slackIdentity;
            return this;
        }

        public Builder owners(final List<AtomistMemberBase> owners) {
            this.owners = owners;
            return this;
        }

        public Builder members(final List<AtomistMemberBase> members) {
            this.members = members;
            return this;
        }

    }

    private List<AtomistMemberBase> owners = new ArrayList<>();

    private List<AtomistMemberBase> members = new ArrayList<>();

    public AtomistTeam(String teamId,
                       String name,
                       String openShiftCloud,
                       TeamSlackIdentity slackIdentity) {
        super(teamId, name, openShiftCloud, slackIdentity);
    }

}
