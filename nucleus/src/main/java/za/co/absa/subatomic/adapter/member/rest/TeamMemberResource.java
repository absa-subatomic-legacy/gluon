package za.co.absa.subatomic.adapter.member.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

import org.springframework.hateoas.ResourceSupport;

@Data
public class TeamMemberResource extends ResourceSupport {

    private String memberId;

    private String firstName;

    private String lastName;

    private String email;

    private String domainUsername;

    private Date joinedAt;

    private Slack slack;

    private List<TeamResource> teams = new ArrayList<>();
}
