package za.co.absa.subatomic.domain.member;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class TeamMember {

    @AggregateIdentifier
    private String memberId;

    private String firstName;

    private String lastName;

    private String email;

    private SlackIdentity slackIdentity;

    private DomainCredentials domainCredentials;

    TeamMember() {
        // required by axon
    }

    @CommandHandler
    public TeamMember(NewTeamMember command) {

        // TODO first check if this member hasn't already joined

        // TODO where do we put roles? Probably on Team

        apply(new TeamMemberCreated(command.getMemberId(),
                command.getFirstName(),
                command.getLastName(),
                command.getEmail(),
                new DomainCredentials(
                        command.getDomainUsername())));
    }

    @CommandHandler
    public TeamMember(NewTeamMemberFromSlack command) {
        NewTeamMember newTeamMember = command.getBasicNewTeamMember();
        apply(new TeamMemberCreated(newTeamMember.getMemberId(),
                newTeamMember.getFirstName(),
                newTeamMember.getLastName(),
                newTeamMember.getEmail(),
                new DomainCredentials(
                        newTeamMember.getDomainUsername()),
                command.getSlackIdentity()));
    }

    @CommandHandler
    void handle(AddSlackDetails command) {
        apply(new SlackIdentityAdded(
                command.getMemberId(),
                command.getScreenName(),
                command.getUserId()));
    }

    @EventSourcingHandler
    void on(TeamMemberCreated event) {
        this.memberId = event.getMemberId();
        this.firstName = event.getFirstName();
        this.lastName = event.getLastName();
        this.email = event.getEmail();
        this.domainCredentials = event.getDomainCredentials();

        event.getSlackIdentity().ifPresent(slack -> this.slackIdentity = slack);
    }

    @EventSourcingHandler
    void on(SlackIdentityAdded event) {
        this.slackIdentity = new SlackIdentity(
                event.getScreenName(),
                event.getUserId());
    }
}
