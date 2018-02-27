package za.co.absa.subatomic.domain.project;

import java.util.Set;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class NewProjectEnvironment {

    @TargetAggregateIdentifier
    private String projectId;

    private TeamMemberId requestedBy;

    private Set<String> allAssociateProjectOwnerAndMemberIds;
}
