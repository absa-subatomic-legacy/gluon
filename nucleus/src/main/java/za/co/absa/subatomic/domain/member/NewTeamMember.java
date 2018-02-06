package za.co.absa.subatomic.domain.member;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class NewTeamMember {

    @TargetAggregateIdentifier
    private String memberId;

    private String firstName;

    private String lastName;

    private String email;

    private String domainUsername;
}
