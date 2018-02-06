package za.co.absa.subatomic.domain.team;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.slugify.Slugify;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class Team {

    @AggregateIdentifier
    private String teamId;

    private String name;

    private String description;

    private Set<TeamMemberId> teamMembers = new HashSet<>();

    private Set<TeamMemberId> owners = new HashSet<>();

    private SlackIdentity slackIdentity;

    private DevOpsEnvironment devOpsEnvironment;

    Team() {
        // required by axon
    }

    @CommandHandler
    public Team(NewTeam command) {
        apply(new TeamCreated(
                command.getTeamId(),
                command.getName(),
                command.getDescription(),
                command.getCreatedBy()));
    }

    @CommandHandler
    public Team(NewTeamFromSlack command) {
        NewTeam newTeam = command.getBasicTeamDetails();
        apply(new TeamCreated(
                newTeam.getTeamId(),
                newTeam.getName(),
                newTeam.getDescription(),
                newTeam.getCreatedBy(),
                command.getSlackIdentity()));
    }

    @EventSourcingHandler
    void on(TeamCreated event) {
        this.teamId = event.getTeamId();
        this.name = event.getName();
        this.description = event.getDescription();
        this.owners.add(event.getCreatedBy());

        event.getSlackIdentity().ifPresent(slack -> this.slackIdentity = slack);
    }

    @CommandHandler
    void when(AddTeamMembers command) {
        Set<TeamMemberId> owners = command.getOwnerMemberIds().stream()
                .map(TeamMemberId::new)
                .collect(Collectors.toSet());

        Set<TeamMemberId> members = command.getTeamMemberIds().stream()
                .map(TeamMemberId::new)
                .collect(Collectors.toSet());

        apply(new TeamMembersAdded(this.teamId, owners, members));
    }

    @EventSourcingHandler
    void on(TeamMembersAdded event) {
        this.owners.addAll(event.getOwners());
        this.teamMembers.addAll(event.getTeamMembers());
    }

    @CommandHandler
    void when(AddSlackIdentity command) {
        apply(new SlackIdentityAdded(
                command.getTeamId(),
                new SlackIdentity(command.getTeamChannel())));
    }

    @EventSourcingHandler
    void on(SlackIdentityAdded event) {
        this.slackIdentity = event.getSlackIdentity();
    }

    @CommandHandler
    void when(NewDevOpsEnvironment command) {
        apply(new DevOpsEnvironmentRequested(
                command.getTeamId(),
                new DevOpsEnvironment(
                        buidDevOpsEnvironmentName(this.name)),
                command.getRequestedBy()));
    }

    @EventSourcingHandler
    void on(DevOpsEnvironmentRequested event) {
        this.devOpsEnvironment = event.getDevOpsEnvironment();
    }

    private String buidDevOpsEnvironmentName(String teamName) {
        return String.format("%s-devops", new Slugify().slugify(teamName));
    }
}
