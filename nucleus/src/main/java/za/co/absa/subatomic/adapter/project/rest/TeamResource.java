package za.co.absa.subatomic.adapter.project.rest;

import lombok.Data;

import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamResource extends ResourceSupport {

    private String teamId;

    private String name;

    private Slack slack;

}
