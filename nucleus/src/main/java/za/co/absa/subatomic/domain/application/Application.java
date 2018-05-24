package za.co.absa.subatomic.domain.application;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

import java.text.MessageFormat;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import za.co.absa.subatomic.domain.exception.ApplicationAuthorisationException;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;

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
        if (!command.getAllAssociateProjectOwnerAndMemberIds()
                .contains(command.getRequestedBy().getTeamMemberId())) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "RequestedBy member {0} is not a valid member of any team associated to the owning project.",
                    command.getRequestedBy()));
        }

        apply(new ApplicationCreated(
                command.getApplicationId(),
                command.getName(),
                command.getDescription(),
                command.getApplicationType(),
                command.getProjectId(),
                command.getRequestedBy(),
                command.getRequestConfiguration(),
                command.getBitbucketRepository()));
    }

    @EventSourcingHandler
    void on(ApplicationCreated event) {
        this.applicationId = event.getApplicationId();
        this.name = event.getName();
        this.description = event.getDescription();
        this.projectId = event.getProjectId();
        this.createdBy = event.getCreatedBy();
        this.bitbucketRepository = event.getBitbucketRepository();
    }
}
