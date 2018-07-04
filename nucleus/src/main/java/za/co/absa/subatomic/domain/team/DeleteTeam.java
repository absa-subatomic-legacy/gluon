package za.co.absa.subatomic.domain.team;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class DeleteTeam {
    @TargetAggregateIdentifier
    String teamId;
}
