package za.co.absa.subatomic.adapter.team.rest;

import lombok.Data;
import org.springframework.hateoas.ResourceSupport;

@Data
public class NewMembershipRequestResource extends ResourceSupport {

    private String requestorMemberId;
}
