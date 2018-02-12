package za.co.absa.subatomic.domain.team;

import lombok.Value;

@Value
public class MembershipRequest {

    private String membershipRequestId;

    private TeamMemberId requestedBy;

    private TeamMemberId approvedBy;

    private MembershipRequestStatus requestStatus;
}
