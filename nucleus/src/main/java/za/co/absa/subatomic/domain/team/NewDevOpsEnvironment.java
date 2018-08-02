package za.co.absa.subatomic.domain.team;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class NewDevOpsEnvironment {

    @TargetAggregateIdentifier
    private String teamId;

    private String messageId;

    private TeamMemberId requestedBy;

}
