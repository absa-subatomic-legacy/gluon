package za.co.absa.subatomic.domain.team;

import java.util.Set;

import lombok.Value;

@Value
public class TeamMembersAdded {

    private String teamId;

    private Set<TeamMemberId> owners;

    private Set<TeamMemberId> teamMembers;
}
