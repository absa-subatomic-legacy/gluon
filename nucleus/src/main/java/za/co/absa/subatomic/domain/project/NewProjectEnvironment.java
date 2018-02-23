package za.co.absa.subatomic.domain.project;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

import java.util.Set;

@Value
public class NewProjectEnvironment {

    @TargetAggregateIdentifier
    private String projectId;

    private TeamMemberId requestedBy;

    private Set<TeamEntity> projectAssociatedTeams;
}
