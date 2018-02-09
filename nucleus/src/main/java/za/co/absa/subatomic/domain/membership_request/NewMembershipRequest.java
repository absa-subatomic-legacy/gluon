package za.co.absa.subatomic.domain.membership_request;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import za.co.absa.subatomic.domain.team.TeamMemberId;

@Value
public class NewMembershipRequest {

    @TargetAggregateIdentifier
    private String requestId;

    private String teamId;

    private final TeamMemberId requestedBy;

}
