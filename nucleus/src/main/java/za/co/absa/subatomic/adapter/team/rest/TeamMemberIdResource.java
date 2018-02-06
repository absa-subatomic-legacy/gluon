package za.co.absa.subatomic.adapter.team.rest;

import lombok.Data;

import org.springframework.hateoas.ResourceSupport;

@Data
public class TeamMemberIdResource extends ResourceSupport {

    private String memberId;
}
