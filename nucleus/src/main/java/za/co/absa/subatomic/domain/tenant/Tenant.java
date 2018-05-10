package za.co.absa.subatomic.domain.tenant;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class Tenant {

    @AggregateIdentifier
    private String tenantId;

    private String name;

    private String description;

    Tenant() {
        // for axon
    }

    @CommandHandler
    public Tenant(NewTenant command) {

        apply(new TenantCreated(
                command.getTenantId(),
                command.getName(),
                command.getDescription()));
    }

    @EventSourcingHandler
    void on(TenantCreated event) {
        this.tenantId = event.getTenantId();
        this.name = event.getName();
        this.description = event.getDescription();
    }

}