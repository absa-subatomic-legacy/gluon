package za.co.absa.subatomic.domain.team;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class AddSlackIdentity {

    @TargetAggregateIdentifier
    private String teamId;

    private String teamChannel;
}
