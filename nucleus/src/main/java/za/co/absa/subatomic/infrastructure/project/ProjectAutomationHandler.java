package za.co.absa.subatomic.infrastructure.project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import za.co.absa.subatomic.domain.member.SlackIdentity;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.project.*;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
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
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantRepository;

@Component
@Slf4j
public class ProjectAutomationHandler {

    private RestTemplate restTemplate;

    private TeamRepository teamRepository;

    private TeamMemberRepository teamMemberRepository;

    private ProjectRepository projectRepository;

    private BitbucketProjectRepository bitbucketProjectRepository;

    private TenantRepository tenantRepository;

    private AtomistConfigurationProperties atomistConfigurationProperties;

    public ProjectAutomationHandler(RestTemplate restTemplate,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            ProjectRepository projectRepository,
            BitbucketProjectRepository bitbucketProjectRepository,
            TenantRepository tenantRepository,
            AtomistConfigurationProperties atomistConfigurationProperties) {
        this.restTemplate = restTemplate;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.projectRepository = projectRepository;
        this.bitbucketProjectRepository = bitbucketProjectRepository;
        this.tenantRepository = tenantRepository;
        this.atomistConfigurationProperties = atomistConfigurationProperties;
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

        za.co.absa.subatomic.domain.team.SlackIdentity teamSlackIdentity = null;
        if (teamEntity.getSlackDetails() != null) {
            teamSlackIdentity = new za.co.absa.subatomic.domain.team.SlackIdentity(
                    teamEntity.getSlackDetails().getTeamChannel());
        }

        Tenant tenant = null;
        TenantEntity tenantEntity = tenantRepository
                .findByTenantId(event.getTenant().getTenantId());
        if (tenantEntity != null) {
            tenant = new Tenant(tenantEntity.getTenantId(),
                    tenantEntity.getName(), tenantEntity.getDescription());
        }

        ProjectCreatedWithDetails newProject = new ProjectCreatedWithDetails(
                event,
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
                        slackIdentity));

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties.getProjectCreatedEventUrl(),
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
                new BitbucketProjectRequest(
                        event.getBitbucketProject().getKey(),
                        event.getBitbucketProject().getName(),
                        event.getBitbucketProject().getDescription()),
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
                tenant,
                new CreatedBy(
                        teamMemberEntity.getMemberId(),
                        teamMemberEntity.getFirstName(),
                        teamMemberEntity.getLastName(),
                        teamMemberEntity.getEmail(),
                        slackIdentity));

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getBitbucketProjectRequestedEventUrl(),
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

        BitbucketProjectEntity bitbucketProjectEntity = bitbucketProjectRepository
                .findByKey(event.getBitbucketProject().getKey());

        sendBitbucketProjectAddedEventToAtomist(event.getProjectId(),
                event.getBitbucketProject(),
                bitbucketProjectEntity.getCreatedBy().getMemberId());
    }

    @EventHandler
    void on(BitbucketProjectLinked event) {
        log.info(
                "A Bitbucket project was linked to project [{}], sending event to Atomist: {}",
                event.getProjectId(), event);

        sendBitbucketProjectAddedEventToAtomist(event.getProjectId(),
                event.getBitbucketProject(),
                event.getRequestedBy().getTeamMemberId());
    }

    void sendBitbucketProjectAddedEventToAtomist(ProjectId projectId,
            za.co.absa.subatomic.domain.project.BitbucketProject bitbucketProject,
            String createdByMemberId) {
        ProjectEntity projectEntity = projectRepository
                .findByProjectId(projectId.getProjectId());

        SlackIdentity slackIdentity = null;
        TeamMemberEntity createdBy = teamMemberRepository
                .findByMemberId(createdByMemberId);
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
                        slackIdentity));

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getBitbucketProjectAddedEventUrl(),
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
                tenant,
                new CreatedBy(
                        createdBy.getMemberId(),
                        createdBy.getFirstName(),
                        createdBy.getLastName(),
                        createdBy.getEmail(),
                        slackIdentity));

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

    @EventHandler
    void on(TeamsLinkedToProject event) {
        log.info(
                "A team was linked to project [{}], sending event to Atomist: {}",
                event.getProjectId(), event);

        ProjectEntity projectEntity = projectRepository
                .findByProjectId(event.getProjectId().getProjectId());

        TeamMemberEntity teamMemberEntity = teamMemberRepository
                .findByMemberId(event.getRequestedBy().getTeamMemberId());

        /*Set<TeamEntity> teamsLinked = new HashSet<>();

        for(TeamId team: event.getTeamsLinked()) {
            teamsLinked.add(teamRepository.findByTeamId(team.getTeamId()));
        }*/

        za.co.absa.subatomic.domain.member.SlackIdentity slackIdentity = null;
        if (teamMemberEntity.getSlackDetails() != null) {
            slackIdentity = new SlackIdentity(
                    teamMemberEntity.getSlackDetails()
                            .getScreenName(),
                    teamMemberEntity.getSlackDetails()
                            .getUserId());
        }

        TeamAssociated teamAssociated = new TeamAssociated(
                projectEntity.getTeams().stream()
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
                atomistConfigurationProperties.getProjectCreatedEventUrl(),
                teamAssociated,
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

        private za.co.absa.subatomic.domain.member.SlackIdentity slackIdentity;
    }

    @Value
    private class TeamMember {

        private String domainUsername;

        private String firstName;

        private SlackIdentity slackIdentity;
    }

    @Value
    private class TeamAssociated {

        private List<Team> team;

        private CreatedBy requestedBy;
    }
}
