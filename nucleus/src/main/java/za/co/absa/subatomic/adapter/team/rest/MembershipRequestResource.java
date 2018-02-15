package za.co.absa.subatomic.adapter.team.rest;

import lombok.Data;
import za.co.absa.subatomic.domain.team.MembershipRequestStatus;

@Data
public class MembershipRequestResource {

    private String membershipRequestId;

    private TeamMemberIdResource requestedBy;

    private TeamMemberIdResource approvedBy;

    private MembershipRequestStatus requestStatus;
}
