package za.co.absa.subatomic.domain.team;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class MembershipRequest {

    @AggregateIdentifier
    private String requestId;

    private String teamId;

    private TeamMemberId requestedBy;

    private TeamMemberId approvedBy;

    private MembershipRequestStatus requestStatus;

    MembershipRequest() {
        // required by axon
    }

    @CommandHandler
    public MembershipRequest(NewMembershipRequest command) {
        apply(new MembershipRequestCreated(
                command.getRequestId(),
                command.getTeamId(),
                command.getRequestedBy()));
    }

    @EventSourcingHandler
    void on(MembershipRequestCreated event) {
        this.requestId = event.getRequestId();
        this.requestedBy = event.getRequestedBy();
        this.teamId = event.getTeamId();
        this.requestStatus = MembershipRequestStatus.OPEN;
    }
}
