package za.co.absa.subatomic.domain.application;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class Application {

    @AggregateIdentifier
    private String applicationId;

    private String name;

    private String description;

    private ApplicationType applicationType;

    private ProjectId projectId;

    private BitbucketGitRepository bitbucketRepository;

    private TeamMemberId createdBy;

    Application() {
        // for axon
    }

    @CommandHandler
    public Application(NewApplication command) {
        apply(new ApplicationCreated(
                command.getApplicationId(),
                command.getName(),
                command.getDescription(),
                command.getApplicationType(),
                command.getProjectId(),
                command.getRequestedBy()));
    }

    @EventSourcingHandler
    void on(ApplicationCreated event) {
        this.applicationId = event.getApplicationId();
        this.name = event.getName();
        this.description = event.getDescription();
        this.projectId = event.getProjectId();
        this.createdBy = event.getCreatedBy();
    }

    @CommandHandler
    void when(RequestApplicationEnvironment command) {
        BitbucketGitRepository bitbucketRepository = command
                .getBitbucketRepository();
        apply(new ApplicationEnvironmentRequested(
                new ApplicationId(command.getApplicationId()),
                command.getName(),
                BitbucketGitRepository.builder()
                        .bitbucketId(bitbucketRepository.getBitbucketId())
                        .slug(bitbucketRepository.getSlug())
                        .name(bitbucketRepository.getName())
                        .repoUrl(bitbucketRepository.getRepoUrl())
                        .remoteUrl(bitbucketRepository.getRemoteUrl())
                        .build(),
                command.getProjectId(),
                command.getRequestedBy()));
    }

    @EventSourcingHandler
    void on(ApplicationEnvironmentRequested event) {
        this.bitbucketRepository = event.getBitbucketGitRepository();
    }
}
