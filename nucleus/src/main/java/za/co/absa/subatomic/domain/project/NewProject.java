package za.co.absa.subatomic.domain.project;

import java.util.Set;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class NewProject {

    @TargetAggregateIdentifier
    private String projectId;

    private String name;

    private String description;

    private TeamMemberId createdBy;

    private TeamId team;

    private TenantId tenant;

    private Set<String> allAssociateProjectOwnerAndMemberIds;
}
