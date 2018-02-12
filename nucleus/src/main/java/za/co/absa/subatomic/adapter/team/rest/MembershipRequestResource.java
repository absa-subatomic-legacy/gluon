package za.co.absa.subatomic.adapter.team.rest;

import org.springframework.hateoas.ResourceSupport;

import lombok.Data;
import za.co.absa.subatomic.domain.team.MembershipRequestStatus;

@Data
public class MembershipRequestResource extends ResourceSupport {

    private String requestedBy;

    private String approvedBy;

    private MembershipRequestStatus requestStatus;
}
