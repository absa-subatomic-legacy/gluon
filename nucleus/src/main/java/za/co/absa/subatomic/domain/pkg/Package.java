package za.co.absa.subatomic.domain.pkg;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class Package {

    @AggregateIdentifier
    private String packageId;

    private PackageType packageType;

    private String name;

    private String description;

    private ProjectId projectId;

    Package() {
        // for axon
    }

    @CommandHandler
    public Package(NewPackage command) {
        PackageType packageType = validatePackageType(command.getPackageType());

        apply(new PackageCreated(
                command.getPackageId(),
                packageType,
                command.getName(),
                command.getDescription(),
                command.getCreatedBy(),
                command.getProject()));
    }

    @EventSourcingHandler
    void on(PackageCreated event) {
        this.packageId = event.getPackageId();
        this.packageType = event.getPackageType();
        this.name = event.getName();
        this.description = event.getDescription();
        this.projectId = event.getProject();
    }

    private PackageType validatePackageType(String packageType) {
        return PackageType.valueOf(packageType);
    }
}