package za.co.absa.subatomic.adapter.team.rest;

import lombok.Data;

import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamMemberIdResource extends ResourceSupport {

    private String memberId;
}
