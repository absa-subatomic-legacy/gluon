package za.co.absa.subatomic.domain.project;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import za.co.absa.subatomic.domain.exception.ApplicationAuthorisationException;
import za.co.absa.subatomic.domain.pkg.ProjectId;

@Aggregate
public class Project {

    @AggregateIdentifier
    private String projectId;

    private String name;

    private String description;

    private BitbucketProject bitbucketProject;

    private Set<TeamId> teams = new HashSet<>();

    Project() {
        // for axon
    }

    @CommandHandler
    public Project(NewProject command) {

        if (!command.getAllAssociateProjectOwnerAndMemberIds()
                .contains(command.getCreatedBy().getTeamMemberId())) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "CreatedBy member {0} is not a valid member the owning team {1}.",
                    command.getCreatedBy(), command.getTeam().getTeamId()));
        }

        apply(new ProjectCreated(
                command.getProjectId(),
                command.getName(),
                command.getDescription(),
                command.getCreatedBy(),
                command.getTeam()));
    }

    @EventSourcingHandler
    void on(ProjectCreated event) {
        this.projectId = event.getProjectId();
        this.name = event.getName();
        this.description = event.getDescription();
        this.teams.add(event.getTeam());
    }

    @CommandHandler
    void when(RequestBitbucketProject command) {
        if (!command.getAllAssociateProjectOwnerAndMemberIds()
                .contains(command.getRequestedBy().getTeamMemberId())) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "RequestedBy member {0} is not a valid member of any team associated to the owning project.",
                    command.getRequestedBy()));
        }
        // TODO check for duplicate keys
        BitbucketProject bitbucketProject = command.getBitbucketProject();
        String key = generateProjectKey(bitbucketProject.getName());

        apply(new BitbucketProjectRequested(
                new ProjectId(command.getProjectId()),
                BitbucketProject.builder()
                        .key(key)
                        .name(bitbucketProject.getName())
                        .description(bitbucketProject.getDescription())
                        .build(),
                command.getRequestedBy()));
    }

    private String generateProjectKey(String projectName) {
        return getFirstLetters(projectName)
                .trim()
                .toUpperCase();
    }

    // see https://stackoverflow.com/a/28461995/2408961
    private String getFirstLetters(String text) {
        StringBuilder firstLetters = new StringBuilder();
        for (String word : text.split(StringUtils.SPACE)) {
            firstLetters.append(word.charAt(0));
        }

        return firstLetters.toString();
    }

    @EventSourcingHandler
    void on(BitbucketProjectRequested event) {
        BitbucketProject bitbucketProject = event.getBitbucketProject();
        this.bitbucketProject = BitbucketProject.builder()
                .key(bitbucketProject.getKey())
                .name(bitbucketProject.getName())
                .description(bitbucketProject.getDescription())
                .build();
    }

    @CommandHandler
    void when(AddBitbucketRepository command) {
        apply(new BitbucketProjectAdded(
                new ProjectId(command.getProjectId()),
                BitbucketProject.builder()
                        .key(this.bitbucketProject.getKey())
                        .id(command.getBitbucketProject().getId())
                        .name(this.bitbucketProject.getName())
                        .description(this.bitbucketProject.getDescription())
                        .url(command.getBitbucketProject().getUrl())
                        .build()));
    }

    @EventSourcingHandler
    void on(BitbucketProjectAdded event) {
        BitbucketProject bitbucketProject = event.getBitbucketProject();
        this.bitbucketProject = BitbucketProject.builder()
                .key(this.bitbucketProject.getKey())
                .id(bitbucketProject.getId())
                .name(this.bitbucketProject.getName())
                .description(this.bitbucketProject.getDescription())
                .url(bitbucketProject.getUrl())
                .build();
    }

    @CommandHandler
    void when(NewProjectEnvironment command) {
        if (!command.getAllAssociateProjectOwnerAndMemberIds()
                .contains(command.getRequestedBy().getTeamMemberId())) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "RequestedBy member {0} is not a valid member of any team associated to the owning project.",
                    command.getRequestedBy()));
        }
        apply(new ProjectEnvironmentRequested(
                command.getProjectId(),
                command.getRequestedBy()));
    }

    @EventSourcingHandler
    void on(ProjectEnvironmentRequested event) {
        // TODO link environments to project
        // will only have meaning when environments are a concept
        // I.e. when people can choose which environments they want
    }

}
