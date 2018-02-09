package za.co.absa.subatomic.adapter.team.rest;

import lombok.Data;
import org.springframework.hateoas.ResourceSupport;
import za.co.absa.subatomic.domain.team.MembershipRequestStatus;

@Data
public class MembershipRequestResource extends ResourceSupport {

    private String teamId;

    private String requestedBy;

    private String approvedBy;

    private MembershipRequestStatus requestStatus;
}
