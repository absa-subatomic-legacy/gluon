package za.co.absa.subatomic.domain.application;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class DeleteApplication {

    @TargetAggregateIdentifier
    private String applicationId;
}
