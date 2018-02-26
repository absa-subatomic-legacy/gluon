package za.co.absa.subatomic.domain.team;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import com.github.slugify.Slugify;

@Aggregate
public class Team {

    @AggregateIdentifier
    private String teamId;
    private String name;

    private String description;

    private Set<TeamMemberId> teamMembers = new HashSet<>();

    private Set<TeamMemberId> owners = new HashSet<>();

    private Set<MembershipRequest> membershipRequests = new HashSet<>();

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

        if (!this.teamMembers.contains(command.getActionedBy())
                && !this.owners.contains(command.getActionedBy())) {
            throw new SecurityException(
                    "createdBy member is not a valid member the owning team.");
        }

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
        if (!this.teamMembers.contains(command.getRequestedBy())
                && !this.owners.contains(command.getRequestedBy())) {
            throw new SecurityException(
                    "requestedBy member is not a valid member the owning team.");
        }
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

    @CommandHandler
    public void when(NewMembershipRequest command) {
        if (this.teamMembers
                .contains(command.getMembershipRequest().getRequestedBy()) ||
                this.owners.contains(
                        command.getMembershipRequest().getRequestedBy())) {
            throw new IllegalArgumentException(
                    "Requesting user is already a member of the team.");
        }
        for (MembershipRequest request : this.membershipRequests) {
            if (request.getRequestStatus() == MembershipRequestStatus.OPEN &&
                    request.getRequestedBy().equals(
                            command.getMembershipRequest().getRequestedBy())) {
                throw new IllegalStateException(
                        "An open membership request already exists for requesting user");
            }
        }
        apply(new MembershipRequestCreated(
                command.getTeamId(),
                command.getMembershipRequest()));
    }

    @EventSourcingHandler
    void on(MembershipRequestCreated event) {
        this.teamId = event.getTeamId();
        this.membershipRequests.add(event.getMembershipRequest());
    }

    @CommandHandler
    public void when(UpdateMembershipRequest command) {
        MembershipRequest existingMembershipRequest = this
                .findMembershipRequestById(command.getMembershipRequest()
                        .getMembershipRequestId());

        if (existingMembershipRequest == null) {
            throw new NullPointerException(MessageFormat.format(
                    "Membership Request with id {0} does not exist for team {1}.",
                    command.getMembershipRequest().getMembershipRequestId(),
                    this.name));
        }

        if (existingMembershipRequest
                .getRequestStatus() != MembershipRequestStatus.OPEN) {
            throw new IllegalStateException(MessageFormat.format(
                    "Cannot update MembershipRequest with id {0} which is not in OPEN state.",
                    command.getMembershipRequest().getMembershipRequestId()));
        }

        if (!this.owners
                .contains(command.getMembershipRequest().getApprovedBy())) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Member {0} is not an owner of team {1} so cannot close request {2}",
                    command.getMembershipRequest().getApprovedBy()
                            .getTeamMemberId(),
                    this.name,
                    command.getMembershipRequest().getMembershipRequestId()));
        }

        if (command.getMembershipRequest()
                .getRequestStatus() == MembershipRequestStatus.APPROVED) {
            Set<TeamMemberId> newOwners = new HashSet<>();
            Set<TeamMemberId> newTeamMembers = new HashSet<>();
            newTeamMembers
                    .add(existingMembershipRequest.getRequestedBy());
            apply(new TeamMembersAdded(teamId, newOwners,
                    newTeamMembers));
        }

        apply(new MembershipRequestUpdated(
                command.getTeamId(),
                existingMembershipRequest,
                command.getMembershipRequest()));
    }

    @EventSourcingHandler
    void on(MembershipRequestUpdated event) {
        this.teamId = event.getTeamId();
        MembershipRequest originalRequest = event
                .getOriginalMembershipRequest();
        MembershipRequest updatedRequest = event.getUpdatedMembershipRequest();

        this.membershipRequests.remove(originalRequest);
        this.membershipRequests.add(
                new MembershipRequest(originalRequest.getMembershipRequestId(),
                        originalRequest.getRequestedBy(),
                        updatedRequest.getApprovedBy(),
                        updatedRequest.getRequestStatus()));
    }

    private String buidDevOpsEnvironmentName(String teamName) {
        return String.format("%s-devops", new Slugify().slugify(teamName));
    }

    private MembershipRequest findMembershipRequestById(String id) {
        MembershipRequest membershipRequest = null;
        for (MembershipRequest existingRequest : this.membershipRequests) {
            if (existingRequest.getMembershipRequestId().equals(id)) {
                membershipRequest = existingRequest;
            }
        }
        return membershipRequest;
    }
}
