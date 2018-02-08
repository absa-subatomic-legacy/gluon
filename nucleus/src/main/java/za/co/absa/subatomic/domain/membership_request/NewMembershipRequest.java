package za.co.absa.subatomic.domain.membership_request;

import lombok.Value;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class NewMembershipRequest {

    private String teamId;

    private final TeamMemberId requestedBy;

    public NewMembershipRequest(String teamId,
                                TeamMemberId requestedBy) {
        this.teamId = teamId;
        this.requestedBy = requestedBy;
    }
}
