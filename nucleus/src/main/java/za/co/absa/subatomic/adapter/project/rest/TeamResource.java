package za.co.absa.subatomic.adapter.project.rest;

import lombok.Data;

import org.springframework.hateoas.ResourceSupport;

@Data
public class TeamResource extends ResourceSupport {

    private String teamId;

    private String name;

    private Slack slack;

}
