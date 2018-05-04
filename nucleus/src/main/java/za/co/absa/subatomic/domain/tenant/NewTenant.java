package za.co.absa.subatomic.domain.tenant;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class NewTenant {

    @TargetAggregateIdentifier
    private String tenantId;

    private String name;

    private String description;
}
