package za.co.absa.subatomic.domain.application;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

import java.util.Set;

@Value
public class NewApplication {

    @TargetAggregateIdentifier
    private String applicationId;

    private String name;

    private String description;

    private ApplicationType applicationType;

    private ProjectId projectId;

    private TeamMemberId requestedBy;

    private Set<TeamEntity> projectAssociatedTeams;
}
