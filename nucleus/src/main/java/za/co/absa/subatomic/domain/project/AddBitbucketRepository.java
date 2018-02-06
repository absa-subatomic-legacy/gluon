package za.co.absa.subatomic.domain.project;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class AddBitbucketRepository {

    @TargetAggregateIdentifier
    private String projectId;

    private BitbucketProject bitbucketProject;
}
