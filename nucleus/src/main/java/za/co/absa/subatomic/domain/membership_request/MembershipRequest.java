package za.co.absa.subatomic.domain.membership_request;

import lombok.Value;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class MembershipRequest {

    private String teamId;

    private TeamMemberId requestedBy;

    private TeamMemberId approvedBy;

    private MembershipRequestStatus requestStatus;

}
