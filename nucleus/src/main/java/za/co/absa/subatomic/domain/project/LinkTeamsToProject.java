package za.co.absa.subatomic.domain.project;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import za.co.absa.subatomic.domain.team.TeamMemberId;

import java.util.Set;

@Value
public class LinkTeamsToProject {

    @TargetAggregateIdentifier
    private String projectId;

    private TeamMemberId requestedBy;

    private Set<TeamId> teamsToLink;

    private Set<String> allAssociateProjectOwnerAndMemberIds;
}
