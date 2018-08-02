package za.co.absa.subatomic.adapter.team.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.hateoas.ResourceSupport;

import lombok.Data;

@Data
public class TeamResource extends ResourceSupport {

    private String teamId;

    private String messageId;

    private String name;

    private String description;

    private Date createdAt;

    private String createdBy;

    private final List<TeamMemberIdResource> members = new ArrayList<>();

    private final List<TeamMemberIdResource> owners = new ArrayList<>();

    private final List<MembershipRequestResource> membershipRequests = new ArrayList<>();

    private Slack slack;

    private DevOpsEnvironment devOpsEnvironment;
}
