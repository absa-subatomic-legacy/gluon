package za.co.absa.subatomic.infrastructure.project;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import za.co.absa.subatomic.domain.member.TeamMemberSlackIdentity;
import za.co.absa.subatomic.domain.project.TeamId;
import za.co.absa.subatomic.domain.project.TenantId;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.domain.team.TeamSlackIdentity;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
import za.co.absa.subatomic.infrastructure.atomist.resource.project.AtomistProject;
import za.co.absa.subatomic.infrastructure.atomist.resource.project.AtomistProjectBase;
import za.co.absa.subatomic.infrastructure.atomist.resource.project.AtomistProjectMapper;
import za.co.absa.subatomic.infrastructure.atomist.resource.team.AtomistTeam;
import za.co.absa.subatomic.infrastructure.atomist.resource.team.AtomistTeamMapper;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;

import java.util.List;
import java.util.stream.Collectors;

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

        AtomistProject projectCreated = new AtomistProjectMapper()
                .createAtomistProject(projectEntity);

        TeamMemberSlackIdentity teamMemberSlackIdentity = null;
        if (teamMemberEntity.getSlackDetails() != null) {
            teamMemberSlackIdentity = new TeamMemberSlackIdentity(
                    teamMemberEntity.getSlackDetails()
                            .getScreenName(),
                    teamMemberEntity.getSlackDetails()
                            .getUserId());
        }

        TeamSlackIdentity teamSlackIdentity = null;
        if (teamEntity.getSlackDetails() != null) {
            teamSlackIdentity = new TeamSlackIdentity(
                    teamEntity.getSlackDetails().getTeamChannel());
        }

        Tenant tenant = new Tenant(tenantEntity.getTenantId(),
                tenantEntity.getName(), tenantEntity.getDescription());

        ProjectCreatedWithDetails newProject = new ProjectCreatedWithDetails(
                projectCreated,
                new AtomistTeam(
                        teamEntity.getTeamId(),
                        teamEntity.getName(),
                        teamEntity.getOpenShiftCloud(),
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
                new AtomistProjectMapper()
                        .createAtomistProjectBase(projectEntity),
                new BitbucketProject(
                        bitbucketProject.getId(),
                        bitbucketProject.getKey(),
                        bitbucketProject.getName(),
                        bitbucketProject.getDescription(),
                        bitbucketProject.getUrl()),
                projectEntity.getTeams().stream()
                        .map(teamEntity -> new AtomistTeam(
                                teamEntity.getTeamId(),
                                teamEntity.getName(),
                                teamEntity.getOpenShiftCloud(),
                                new TeamSlackIdentity(
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

        ProjectEnvironmentsRequestedWithDetails projectEnvironmentsRequested = new ProjectEnvironmentsRequestedWithDetails(
                new AtomistProjectMapper()
                        .createAtomistProject(projectEntity),
                null,
                projectEntity.getTeams().stream()
                        .map(teamEntity ->
                                new AtomistTeamMapper().createAtomistTeam(teamEntity)
                        )
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
                projectEntity.getProjectId(), projectEnvironmentsRequested);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getProjectEnvironmentsRequestedEventUrl(),
                projectEnvironmentsRequested,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    public void teamsLinkedToProject(ProjectEntity projectEntity,
                                     TeamMemberEntity teamMemberEntity, List<TeamEntity> teamEntities) {

        TeamMemberSlackIdentity teamMemberSlackIdentity = null;
        if (teamMemberEntity.getSlackDetails() != null) {
            teamMemberSlackIdentity = new TeamMemberSlackIdentity(
                    teamMemberEntity.getSlackDetails()
                            .getScreenName(),
                    teamMemberEntity.getSlackDetails()
                            .getUserId());
        }

        List<AtomistTeam> currentTeams = teamEntities.stream()
                .map(teamEntity -> new AtomistTeam(
                        teamEntity.getTeamId(),
                        teamEntity.getName(),
                        teamEntity.getOpenShiftCloud(),
                        new TeamSlackIdentity(
                                teamEntity.getSlackDetails()
                                        .getTeamChannel())))
                .collect(Collectors.toList());

        AtomistProjectBase project = new AtomistProjectBase.Builder()
                .projectId(projectEntity.getProjectId())
                .name(projectEntity.getName())
                .description(projectEntity.getDescription())
                .createdBy(new TeamMemberId(
                        projectEntity.getCreatedBy().getMemberId()))
                .team(new TeamId(projectEntity.getOwningTeam().getTeamId()))
                .tenant(new TenantId(
                        projectEntity.getOwningTenant().getTenantId()))
                .build();

        TeamAssociated teamAssociated = new TeamAssociated(
                project,
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

        private AtomistProjectBase project;

        private AtomistTeam team;

        private Tenant tenant;

        private CreatedBy createdBy;
    }

    @Value
    private class ProjectEnvironmentsRequestedWithDetails {

        private AtomistProject project;

        private BitbucketProjectRequest bitbucketProjectRequest;

        private List<AtomistTeam> teams;

        private Tenant owningTenant;

        private CreatedBy requestedBy;
    }

    @Value
    private class BitbucketProjectCreatedWithDetails {

        private AtomistProjectBase project;

        private BitbucketProject bitbucketProject;

        private List<AtomistTeam> teams;

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
    private class TeamAssociated {

        private AtomistProjectBase project;

        private List<AtomistTeam> teams;

        private CreatedBy requestedBy;
    }
}
