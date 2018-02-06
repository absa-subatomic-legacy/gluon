package za.co.absa.subatomic.domain.member;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class AddSlackDetails {

    @TargetAggregateIdentifier
    private String memberId;

    private String screenName;

    private String userId;
}
