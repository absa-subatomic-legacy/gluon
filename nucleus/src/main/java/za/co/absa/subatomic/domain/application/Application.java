package za.co.absa.subatomic.domain.application;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

import java.util.Collection;
import java.util.Set;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

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
        if(!requesterIsMemberOfAssociatedTeam(command.getRequestedBy(), command.getProjectAssociatedTeams())){
            throw new SecurityException("requestedBy member is not a valid member of any team associated to the owning project.");
        }

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
        if(!requesterIsMemberOfAssociatedTeam(command.getRequestedBy(), command.getProjectAssociatedTeams())){
            throw new SecurityException("requestedBy member is not a valid member of any team associated to the owning project.");
        }
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
