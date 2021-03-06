package za.co.absa.subatomic.adapter.team.rest;

import java.util.Date;

import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

import lombok.Data;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamResourceBase extends ResourceSupport {

    private String teamId;

    private String name;

    private String description;

    private String openShiftCloud;

    private Date createdAt;

    private String createdBy;

    private Slack slack;
}
