package za.co.absa.subatomic.domain.project;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@Value
public class NewProject {

    @TargetAggregateIdentifier
    private String projectId;

    private String name;

    private String description;

    private TeamMemberId createdBy;

    private TeamEntity team;
}
