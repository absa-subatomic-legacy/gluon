package za.co.absa.subatomic.application.team;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import za.co.absa.subatomic.domain.exception.ApplicationAuthorisationException;
import za.co.absa.subatomic.domain.exception.InvalidRequestException;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

public class TeamAssertions {

    public void assertMemberBelongsToAnyTeam(TeamMemberEntity teamMemberEntity,
            Collection<TeamEntity> teams) {
        if (!this.memberBelongsToAnyTeam(teamMemberEntity, teams)) {
            throw new ApplicationAuthorisationException((MessageFormat.format(
                    "TeamMember with id {0} is not a member of any valid team.",
                    teamMemberEntity.getMemberId())));
        }
    }

    public boolean memberBelongsToAnyTeam(TeamMemberEntity memberEntity,
            Collection<TeamEntity> teams) {

        boolean memberBelongsToATeam = false;

        for (TeamEntity team : teams) {
            if (this.memberBelongsToTeam(memberEntity, team)) {
                memberBelongsToATeam = true;
                break;
            }
        }

        return memberBelongsToATeam;
    }

    public void assertMemberBelongsToTeam(TeamMemberEntity memberEntity,
            TeamEntity teamEntity) {
        if (!this.memberBelongsToTeam(memberEntity, teamEntity)) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "TeamMember with id {0} is not a member of the team with id {1}.",
                    memberEntity.getMemberId(),
                    teamEntity.getTeamId()));
        }
    }

    public void assertMemberDoesNotBelongToTeam(TeamMemberEntity memberEntity,
            TeamEntity teamEntity) {
        if (this.memberBelongsToTeam(memberEntity, teamEntity)) {
            throw new InvalidRequestException(MessageFormat.format(
                    "TeamMember with id {0} is already a member of the team with id {1}.",
                    memberEntity.getMemberId(),
                    teamEntity.getTeamId()));
        }
    }

    public void assertMemberIsOwnerOfTeam(TeamMemberEntity memberEntity,
            TeamEntity teamEntity) {
        if (teamEntity.getOwners().stream().map(TeamMemberEntity::getMemberId)
                .noneMatch(memberEntity.getMemberId()::equals)) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "TeamMember with id {0} is not an owner of the team with id {1}.",
                    memberEntity.getMemberId(),
                    teamEntity.getTeamId()));
        }
    }

    private boolean memberBelongsToTeam(TeamMemberEntity memberEntity,
            TeamEntity teamEntity) {
        Set<TeamMemberEntity> allMembers = new HashSet<>();
        allMembers.addAll(teamEntity.getMembers());
        allMembers.addAll(teamEntity.getOwners());

        return allMembers.stream().map(TeamMemberEntity::getMemberId)
                .anyMatch(memberEntity.getMemberId()::equals);
    }
}
