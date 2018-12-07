package za.co.absa.subatomic.infrastructure.project;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.domain.member.TeamMemberSlackIdentity;
import za.co.absa.subatomic.domain.project.ProjectCreated;
import za.co.absa.subatomic.domain.project.TeamId;
import za.co.absa.subatomic.domain.project.TenantId;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;

@Component
@Slf4j
public class ProjectAutomationHandler {

    private RestTemplate restTemplate;

    private AtomistConfigurationProperties atomistConfigurationProperties;

    public ProjectAutomationHandler(RestTemplate restTemplate,
            AtomistConfigurationProperties atomistConfigurationProperties) {
        this.restTemplate = restTemplate;
        this.atomistConfigurationProperties = atomistConfigurationProperties;
    }

    public void projectCreated(ProjectEntity projectEntity,
            TeamEntity teamEntity,
            TeamMemberEntity teamMemberEntity, TenantEntity tenantEntity) {

        ProjectCreated projectCreated = ProjectCreated.builder()
                .projectId(projectEntity.getProjectId())
                .name(projectEntity.getName())
                .description(projectEntity.getDescription())
                .createdBy(new TeamMemberId(teamMemberEntity.getMemberId()))
                .tenant(new TenantId(tenantEntity.getTenantId()))
                .team(new TeamId(teamEntity.getTeamId()))
                .build();

        TeamMemberSlackIdentity teamMemberSlackIdentity = null;
        if (teamMemberEntity.getSlackDetails() != null) {
            teamMemberSlackIdentity = new TeamMemberSlackIdentity(
                    teamMemberEntity.getSlackDetails()
                            .getScreenName(),
                    teamMemberEntity.getSlackDetails()
                            .getUserId());
        }

        za.co.absa.subatomic.domain.team.SlackIdentity teamSlackIdentity = null;
        if (teamEntity.getSlackDetails() != null) {
            teamSlackIdentity = new za.co.absa.subatomic.domain.team.SlackIdentity(
                    teamEntity.getSlackDetails().getTeamChannel());
        }

        Tenant tenant = new Tenant(tenantEntity.getTenantId(),
                tenantEntity.getName(), tenantEntity.getDescription());

        ProjectCreatedWithDetails newProject = new ProjectCreatedWithDetails(
                projectCreated,
                new Team(
                        teamEntity.getTeamId(),
                        teamEntity.getName(),
                        teamSlackIdentity),
                tenant,
                new CreatedBy(
                        teamMemberEntity.getMemberId(),
                        teamMemberEntity.getFirstName(),
                        teamMemberEntity.getLastName(),
                        teamMemberEntity.getEmail(),
                        teamMemberSlackIdentity));

        log.info("A project was created, sending event to Atomist: {}",
                newProject);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties.getProjectCreatedEventUrl(),
                newProject,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    public void bitbucketProjectLinkedToProject(ProjectEntity projectEntity,
            za.co.absa.subatomic.domain.project.BitbucketProject bitbucketProject,
            TeamMemberEntity createdBy) {

        TeamMemberSlackIdentity teamMemberSlackIdentity = null;
        if (createdBy.getSlackDetails() != null) {
            teamMemberSlackIdentity = new TeamMemberSlackIdentity(
                    createdBy.getSlackDetails()
                            .getScreenName(),
                    createdBy.getSlackDetails()
                            .getUserId());
        }

        BitbucketProjectCreatedWithDetails bitbucketProjectLinked = new BitbucketProjectCreatedWithDetails(
                ProjectCreated.builder()
                        .projectId(projectEntity.getProjectId())
                        .name(projectEntity.getName())
                        .description(projectEntity.getDescription())
                        .build(),
                new BitbucketProject(
                        bitbucketProject.getId(),
                        bitbucketProject.getKey(),
                        bitbucketProject.getName(),
                        bitbucketProject.getDescription(),
                        bitbucketProject.getUrl()),
                projectEntity.getTeams().stream()
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
                        teamMemberSlackIdentity));
        log.info(
                "A Bitbucket project was linked to project [{}], sending event to Atomist: {}",
                projectEntity.getProjectId(), bitbucketProjectLinked);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getBitbucketProjectAddedEventUrl(),
                bitbucketProjectLinked,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    public void requestProjectEnvironment(ProjectEntity projectEntity,
            TeamMemberEntity createdBy) {

        TeamMemberSlackIdentity teamMemberSlackIdentity = null;
        if (createdBy.getSlackDetails() != null) {
            teamMemberSlackIdentity = new TeamMemberSlackIdentity(
                    createdBy.getSlackDetails()
                            .getScreenName(),
                    createdBy.getSlackDetails()
                            .getUserId());
        }

        Tenant tenant = null;
        TenantEntity tenantEntity = projectEntity.getOwningTenant();
        if (tenantEntity != null) {
            tenant = new Tenant(tenantEntity.getTenantId(),
                    tenantEntity.getName(), tenantEntity.getDescription());
        }

        BitbucketProjectRequestedWithDetails bitbucketProjectRequested = new BitbucketProjectRequestedWithDetails(
                ProjectCreated.builder()
                        .projectId(projectEntity.getProjectId())
                        .name(projectEntity.getName())
                        .description(projectEntity.getDescription())
                        .build(),
                null,
                projectEntity.getTeams().stream()
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
                                                    new TeamMemberSlackIdentity(
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
                                                    new TeamMemberSlackIdentity(
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
                tenant,
                new CreatedBy(
                        createdBy.getMemberId(),
                        createdBy.getFirstName(),
                        createdBy.getLastName(),
                        createdBy.getEmail(),
                        teamMemberSlackIdentity));

        log.info(
                "OpenShift project environments were requested [{}], sending event to Atomist: {}",
                projectEntity.getProjectId(), bitbucketProjectRequested);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getProjectEnvironmentsRequestedEventUrl(),
                bitbucketProjectRequested,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    public void teamsLinkedToProject(ProjectEntity projectEntity,
            TeamMemberEntity teamMemberEntity) {

        TeamMemberSlackIdentity teamMemberSlackIdentity = null;
        if (teamMemberEntity.getSlackDetails() != null) {
            teamMemberSlackIdentity = new TeamMemberSlackIdentity(
                    teamMemberEntity.getSlackDetails()
                            .getScreenName(),
                    teamMemberEntity.getSlackDetails()
                            .getUserId());
        }

        List<Team> currentTeams = projectEntity.getTeams().stream()
                .map(teamEntity -> new Team(
                        teamEntity.getTeamId(),
                        teamEntity.getName(),
                        new za.co.absa.subatomic.domain.team.SlackIdentity(
                                teamEntity.getSlackDetails()
                                        .getTeamChannel())))
                .collect(Collectors.toList());

        TeamAssociated teamAssociated = new TeamAssociated(
                currentTeams,
                new CreatedBy(
                        teamMemberEntity.getMemberId(),
                        teamMemberEntity.getFirstName(),
                        teamMemberEntity.getLastName(),
                        teamMemberEntity.getEmail(),
                        teamMemberSlackIdentity));

        log.info(
                "A team was linked to project [{}], sending event to Atomist: {}",
                projectEntity.getProjectId(), teamAssociated);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getTeamsLinkedToProjectEventUrl(),
                teamAssociated,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info(
                    "Atomist has ingested team associated event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @Value
    private class ProjectCreatedWithDetails {

        private ProjectCreated project;

        private Team team;

        private Tenant tenant;

        private CreatedBy createdBy;
    }

    @Value
    private class BitbucketProjectRequestedWithDetails {

        private ProjectCreated project;

        private BitbucketProjectRequest bitbucketProjectRequest;

        private List<Team> teams;

        private Tenant owningTenant;

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
    private class Tenant {

        private String tenantId;

        private String name;

        private String description;
    }

    @Value
    private class CreatedBy {

        private String memberId;

        private String firstName;

        private String lastName;

        private String email;

        private TeamMemberSlackIdentity slackIdentity;
    }

    @Value
    private class TeamMember {

        private String domainUsername;

        private String firstName;

        private TeamMemberSlackIdentity slackIdentity;
    }

    @Value
    private class TeamAssociated {

        private List<Team> team;

        private CreatedBy requestedBy;
    }
}
