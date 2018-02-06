package za.co.absa.subatomic.domain.team;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class NewTeam {

    @TargetAggregateIdentifier
    private String teamId;

    private String name;

    private String description;

    private TeamMemberId createdBy;
}
