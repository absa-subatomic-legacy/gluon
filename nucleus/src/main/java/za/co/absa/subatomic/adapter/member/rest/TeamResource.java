package za.co.absa.subatomic.adapter.member.rest;

import lombok.Data;

import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

@EqualsAndHashCode(callSuper = true)
@Data
class TeamResource extends ResourceSupport {

    private String name;

    private za.co.absa.subatomic.adapter.team.rest.Slack slack;
}
