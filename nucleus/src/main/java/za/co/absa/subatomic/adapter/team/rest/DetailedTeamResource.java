package za.co.absa.subatomic.adapter.team.rest;

import lombok.Data;
import za.co.absa.subatomic.adapter.member.rest.TeamMemberResource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class DetailedTeamResource {
    private String teamId;

    private String name;

    private String description;

    private Date createdAt;

    private String createdBy;

    private final List<TeamMemberResource> members = new ArrayList<>();

    private final List<TeamMemberResource> owners = new ArrayList<>();

    private final List<MembershipRequestResource> membershipRequests = new ArrayList<>();

    private Slack slack;
}
