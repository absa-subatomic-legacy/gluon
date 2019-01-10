package za.co.absa.subatomic.infrastructure.atomist.resource.team;

import java.util.stream.Collectors;

import za.co.absa.subatomic.domain.member.TeamMemberSlackIdentity;
import za.co.absa.subatomic.domain.team.TeamSlackIdentity;
import za.co.absa.subatomic.infrastructure.atomist.resource.AtomistMemberBase;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

public class AtomistTeamMapper {

    public AtomistTeam createAtomistTeam(TeamEntity teamEntity) {

        TeamSlackIdentity teamEntitySlackIdentity = null;
        if (teamEntity.getSlackDetails() != null) {
            teamEntitySlackIdentity = new TeamSlackIdentity(
                    teamEntity.getSlackDetails()
                            .getTeamChannel());
        }

        return new AtomistTeam.Builder()
                .teamId(teamEntity.getTeamId())
                .name(teamEntity.getName())
                .slackIdentity(teamEntitySlackIdentity)
                .openShiftCloud(teamEntity.getOpenShiftCloud())
                .members(teamEntity.getMembers().stream()
                        .map(memberEntity -> new AtomistMemberBase(
                                memberEntity.getFirstName(),
                                memberEntity.getDomainUsername(),
                                new TeamMemberSlackIdentity(
                                        memberEntity.getSlackDetails()
                                                .getScreenName(),
                                        memberEntity.getSlackDetails()
                                                .getUserId())))
                        .collect(Collectors.toList()))
                .owners(teamEntity.getOwners().stream()
                        .map(memberEntity -> new AtomistMemberBase(
                                memberEntity.getFirstName(),
                                memberEntity.getDomainUsername(),
                                new TeamMemberSlackIdentity(
                                        memberEntity.getSlackDetails()
                                                .getScreenName(),
                                        memberEntity.getSlackDetails()
                                                .getUserId())))
                        .collect(Collectors.toList()))
                .build();
    }
}
