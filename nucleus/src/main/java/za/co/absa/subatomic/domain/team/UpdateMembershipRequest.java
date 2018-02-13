package za.co.absa.subatomic.domain.team;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class UpdateMembershipRequest {

    @TargetAggregateIdentifier
    private String teamId;

    private MembershipRequest membershipRequest;

}
