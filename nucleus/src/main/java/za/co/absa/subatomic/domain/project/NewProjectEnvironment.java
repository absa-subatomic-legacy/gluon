package za.co.absa.subatomic.domain.project;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class NewProjectEnvironment {

    @TargetAggregateIdentifier
    private String projectId;

    private TeamMemberId requestedBy;
}
