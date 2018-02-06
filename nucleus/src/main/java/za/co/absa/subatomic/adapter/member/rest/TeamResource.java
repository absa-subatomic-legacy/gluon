package za.co.absa.subatomic.adapter.member.rest;

import lombok.Data;

import org.springframework.hateoas.ResourceSupport;

@Data
public class TeamResource extends ResourceSupport {

    private String name;

    private za.co.absa.subatomic.adapter.team.rest.Slack slack;
}
