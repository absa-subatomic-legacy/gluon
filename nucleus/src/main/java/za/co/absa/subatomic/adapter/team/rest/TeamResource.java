package za.co.absa.subatomic.adapter.team.rest;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import za.co.absa.subatomic.adapter.member.rest.TeamMemberResourceBase;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamResource extends TeamResourceBase {

    private final List<TeamMemberResourceBase> members = new ArrayList<>();

    private final List<TeamMemberResourceBase> owners = new ArrayList<>();

    private final List<MembershipRequestResource> membershipRequests = new ArrayList<>();

    private DevOpsEnvironment devOpsEnvironment;
}
