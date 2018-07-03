package za.co.absa.subatomic.domain.project;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class DeleteProject {

    @TargetAggregateIdentifier
    private String projectId;
}
