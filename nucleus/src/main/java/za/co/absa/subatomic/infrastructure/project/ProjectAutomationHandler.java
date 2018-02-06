package za.co.absa.subatomic.infrastructure.project;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import za.co.absa.subatomic.domain.member.SlackIdentity;
import za.co.absa.subatomic.domain.project.BitbucketProjectAdded;
import za.co.absa.subatomic.domain.project.BitbucketProjectRequested;
import za.co.absa.subatomic.domain.project.ProjectCreated;
import za.co.absa.subatomic.domain.project.ProjectEnvironmentRequested;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;
import za.co.absa.subatomic.infrastructure.project.view.jpa.BitbucketProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.BitbucketProjectRepository;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ProjectAutomationHandler {

    private RestTemplate restTemplate;

    private TeamRepository teamRepository;

    private TeamMemberRepository teamMemberRepository;

    private ProjectRepository projectRepository;

    private BitbucketProjectRepository bitbucketProjectRepository;

    public ProjectAutomationHandler(RestTemplate restTemplate,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            ProjectRepository projectRepository,
            BitbucketProjectRepository bitbucketProjectRepository) {
        this.restTemplate = restTemplate;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.projectRepository = projectRepository;
        this.bitbucketProjectRepository = bitbucketProjectRepository;
    }

    @EventHandler
    void on(ProjectCreated event) {
        log.info("A project was created, sending event to Atomist: {}",
                event);

        TeamEntity teamEntity = teamRepository
                .findByTeamId(event.getTeam().getTeamId());

        TeamMemberEntity teamMemberEntity = teamMemberRepository
                .findByMemberId(event.getCreatedBy().getTeamMemberId());

        SlackIdentity slackIdentity = null;
        if (teamMemberEntity.getSlackDetails() != null) {
            slackIdentity = new SlackIdentity(
                    teamMemberEntity.getSlackDetails()
                            .getScreenName(),
                    teamMemberEntity.getSlackDetails()
                            .getUserId());
        }

        ProjectCreatedWithDetails newProject = new ProjectCreatedWithDetails(
                event,
                new Team(
                        teamEntity.getTeamId(),
                        teamEntity.getName(),
                        new za.co.absa.subatomic.domain.team.SlackIdentity(
                                teamEntity.getSlackDetails().getTeamChannel())),
                new CreatedBy(
                        teamMemberEntity.getMemberId(),
                        teamMemberEntity.getFirstName(),
                        teamMemberEntity.getLastName(),
                        teamMemberEntity.getEmail(),
                        slackIdentity));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://webhook.atomist.com/atomist/teams/T8RGCS6T0/ingestion/ProjectCreatedEvent/6941caa4-c0fd-487c-baf6-292014c89e45",
                newProject,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @EventHandler
    void on(BitbucketProjectRequested event) {
        log.info(
                "A Bitbucket project was requested for project [{}], sending event to Atomist: {}",
                event.getProjectId(), event);

        ProjectEntity projectEntity = projectRepository
                .findByProjectId(event.getProjectId().getProjectId());

        TeamMemberEntity teamMemberEntity = teamMemberRepository
                .findByMemberId(event.getRequestedBy().getTeamMemberId());

        SlackIdentity slackIdentity = null;
        if (teamMemberEntity.getSlackDetails() != null) {
            slackIdentity = new SlackIdentity(
                    teamMemberEntity.getSlackDetails()
                            .getScreenName(),
                    teamMemberEntity.getSlackDetails()
                            .getUserId());
        }

        BitbucketProjectRequestedWithDetails bitbucketProjectRequested = new BitbucketProjectRequestedWithDetails(
                ProjectCreated.builder()
                        .projectId(projectEntity.getProjectId())
                        .name(projectEntity.getName())
                        .description(projectEntity.getDescription())
                        .build(),
                new BitbucketProjectRequest(
                        event.getBitbucketProject().getKey(),
                        event.getBitbucketProject().getName(),
                        event.getBitbucketProject().getDescription()),
                teamMemberEntity.getTeams().stream()
                        .map(teamEntity -> new Team(
                                teamEntity.getTeamId(),
                                teamEntity.getName(),
                                new za.co.absa.subatomic.domain.team.SlackIdentity(
                                        teamEntity.getSlackDetails()
                                                .getTeamChannel())))
                        .collect(Collectors.toList()),
                new CreatedBy(
                        teamMemberEntity.getMemberId(),
                        teamMemberEntity.getFirstName(),
                        teamMemberEntity.getLastName(),
                        teamMemberEntity.getEmail(),
                        slackIdentity));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://webhook.atomist.com/atomist/teams/T8RGCS6T0/ingestion/BitbucketProjectRequestedEvent/1e68e580-256d-4523-911d-9080e87753dd",
                bitbucketProjectRequested,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @EventHandler
    void on(BitbucketProjectAdded event) {
        log.info(
                "A Bitbucket project was added to project [{}], sending event to Atomist: {}",
                event.getProjectId(), event);

        ProjectEntity projectEntity = projectRepository
                .findByProjectId(event.getProjectId().getProjectId());

        BitbucketProjectEntity bitbucketProjectEntity = bitbucketProjectRepository
                .findByKey(event.getBitbucketProject().getKey());

        SlackIdentity slackIdentity = null;
        TeamMemberEntity createdBy = bitbucketProjectEntity.getCreatedBy();
        if (createdBy.getSlackDetails() != null) {
            slackIdentity = new SlackIdentity(
                    createdBy.getSlackDetails()
                            .getScreenName(),
                    createdBy.getSlackDetails()
                            .getUserId());
        }

        BitbucketProjectCreatedWithDetails bitbucketProjectRequested = new BitbucketProjectCreatedWithDetails(
                ProjectCreated.builder()
                        .projectId(projectEntity.getProjectId())
                        .name(projectEntity.getName())
                        .description(projectEntity.getDescription())
                        .build(),
                new BitbucketProject(
                        event.getBitbucketProject().getId(),
                        event.getBitbucketProject().getKey(),
                        event.getBitbucketProject().getName(),
                        event.getBitbucketProject().getDescription(),
                        event.getBitbucketProject().getUrl()),
                createdBy.getTeams().stream()
                        .map(teamEntity -> new Team(
                                teamEntity.getTeamId(),
                                teamEntity.getName(),
                                new za.co.absa.subatomic.domain.team.SlackIdentity(
                                        teamEntity.getSlackDetails()
                                                .getTeamChannel())))
                        .collect(Collectors.toList()),
                new CreatedBy(
                        createdBy.getMemberId(),
                        createdBy.getFirstName(),
                        createdBy.getLastName(),
                        createdBy.getEmail(),
                        slackIdentity));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://webhook.atomist.com/atomist/teams/T8RGCS6T0/ingestion/BitbucketProjectAddedEvent/cb30a072-ba62-4f08-b89f-00a4f24304ba",
                bitbucketProjectRequested,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @EventHandler
    void on(ProjectEnvironmentRequested event) {
        log.info(
                "OpenShift project environments were requested [{}], sending event to Atomist: {}",
                event.getProjectId(), event);

        ProjectEntity projectEntity = projectRepository
                .findByProjectId(event.getProjectId());

        SlackIdentity slackIdentity = null;
        TeamMemberEntity createdBy = teamMemberRepository
                .findByMemberId(event.getRequestedBy().getTeamMemberId());
        if (createdBy.getSlackDetails() != null) {
            slackIdentity = new SlackIdentity(
                    createdBy.getSlackDetails()
                            .getScreenName(),
                    createdBy.getSlackDetails()
                            .getUserId());
        }

        BitbucketProjectRequestedWithDetails bitbucketProjectRequested = new BitbucketProjectRequestedWithDetails(
                ProjectCreated.builder()
                        .projectId(projectEntity.getProjectId())
                        .name(projectEntity.getName())
                        .description(projectEntity.getDescription())
                        .build(),
                null,
                createdBy.getTeams().stream()
                        .map(teamEntity -> {
                            Team team = new Team(
                                    teamEntity.getTeamId(),
                                    teamEntity.getName(),
                                    new za.co.absa.subatomic.domain.team.SlackIdentity(
                                            teamEntity.getSlackDetails()
                                                    .getTeamChannel()));
                            team.getOwners().addAll(
                                    teamEntity.getOwners().stream()
                                            .map(memberEntity -> new TeamMember(
                                                    memberEntity
                                                            .getDomainUsername(),
                                                    memberEntity
                                                            .getFirstName(),
                                                    new SlackIdentity(
                                                            memberEntity
                                                                    .getSlackDetails()
                                                                    .getScreenName(),
                                                            memberEntity
                                                                    .getSlackDetails()
                                                                    .getUserId())))
                                            .collect(Collectors.toList()));
                            team.getMembers().addAll(
                                    teamEntity.getMembers().stream()
                                            .map(memberEntity -> new TeamMember(
                                                    memberEntity
                                                            .getDomainUsername(),
                                                    memberEntity.getFirstName(),
                                                    new SlackIdentity(
                                                            memberEntity
                                                                    .getSlackDetails()
                                                                    .getScreenName(),
                                                            memberEntity
                                                                    .getSlackDetails()
                                                                    .getUserId())))
                                            .collect(Collectors.toList()));
                            return team;
                        })
                        .collect(Collectors.toList()),
                new CreatedBy(
                        createdBy.getMemberId(),
                        createdBy.getFirstName(),
                        createdBy.getLastName(),
                        createdBy.getEmail(),
                        slackIdentity));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://webhook.atomist.com/atomist/teams/T8RGCS6T0/ingestion/ProjectEnvironmentsRequestedEvent/fe93f013-8547-45f8-92fd-6e4b145de3da",
                bitbucketProjectRequested,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @Value
    private class ProjectCreatedWithDetails {

        private ProjectCreated project;

        private Team team;

        private CreatedBy createdBy;
    }

    @Value
    private class BitbucketProjectRequestedWithDetails {

        private ProjectCreated project;

        private BitbucketProjectRequest bitbucketProjectRequest;

        private List<Team> teams;

        private CreatedBy requestedBy;
    }

    @Value
    private class BitbucketProjectCreatedWithDetails {

        private ProjectCreated project;

        private BitbucketProject bitbucketProject;

        private List<Team> teams;

        private CreatedBy createdBy;
    }

    @Value
    private class BitbucketProjectRequest {

        private String key;

        private String name;

        private String description;
    }

    @Value
    private class BitbucketProject {

        private String id;

        private String key;

        private String name;

        private String description;

        private String url;
    }

    @Value
    private class Team {

        private String teamId;

        private String name;

        private za.co.absa.subatomic.domain.team.SlackIdentity slackIdentity;

        private final List<TeamMember> owners = new ArrayList<>();

        private final List<TeamMember> members = new ArrayList<>();
    }

    @Value
    private class CreatedBy {

        private String memberId;

        private String firstName;

        private String lastName;

        private String email;

        private za.co.absa.subatomic.domain.member.SlackIdentity slackIdentity;
    }

    @Value
    private class TeamMember {

        private String domainUsername;

        private String firstName;

        private SlackIdentity slackIdentity;
    }
}
