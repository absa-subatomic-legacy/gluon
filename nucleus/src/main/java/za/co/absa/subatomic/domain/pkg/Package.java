package za.co.absa.subatomic.domain.pkg;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

import java.util.Collection;
import java.util.Set;

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

        if(!requesterIsMemberOfAssociatedTeam(command.getCreatedBy(), command.getProjectAssociatedTeams())){
            throw new SecurityException("createdBy member is not a valid member of any team associated to the owning project.");
        }

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

    private boolean requesterIsMemberOfAssociatedTeam(TeamMemberId requester,
                                                      Set<TeamEntity> projectAssociatedTeams) {
        for (TeamEntity team : projectAssociatedTeams) {
            if (memberBelongsToTeam(requester, team)) {
                return true;
            }
        }
        return false;
    }

    private boolean memberBelongsToTeam(TeamMemberId member, TeamEntity team) {
        return memberInMemberList(member, team.getMembers())
                || memberInMemberList(member, team.getOwners());
    }

    private boolean memberInMemberList(TeamMemberId member,
                                       Collection<TeamMemberEntity> memberList) {
        for (TeamMemberEntity memberEntity : memberList) {
            if (memberEntity.getMemberId().equals(member.getTeamMemberId())) {
                return true;
            }
        }
        return false;
    }
}
