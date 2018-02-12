package za.co.absa.subatomic.domain.team;

import lombok.Value;

@Value
public class MembershipRequestCreated {

    private String requestId;

    private String teamId;

    private final TeamMemberId requestedBy;

    public MembershipRequestCreated(String requestId, String teamId, TeamMemberId requestedBy) {
        this.requestId = requestId;
        this.teamId = teamId;
        this.requestedBy = requestedBy;
    }
}
