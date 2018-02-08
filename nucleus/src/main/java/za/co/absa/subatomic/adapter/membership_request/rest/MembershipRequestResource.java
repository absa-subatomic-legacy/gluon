package za.co.absa.subatomic.adapter.membership_request.rest;

import lombok.Data;
import org.springframework.hateoas.ResourceSupport;
import za.co.absa.subatomic.domain.membership_request.MembershipRequestStatus;

@Data
public class MembershipRequestResource extends ResourceSupport {

    private String teamId;

    private String requestedBy;

    private String approvedBy;

    private MembershipRequestStatus requestStatus;
}
