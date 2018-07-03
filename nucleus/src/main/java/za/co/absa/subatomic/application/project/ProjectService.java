package za.co.absa.subatomic.application.project;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.adapter.project.rest.TeamResource;
import za.co.absa.subatomic.application.tenant.TenantService;
import za.co.absa.subatomic.domain.exception.DuplicateRequestException;
import za.co.absa.subatomic.domain.exception.InvalidRequestException;
import za.co.absa.subatomic.domain.project.*;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamRepository;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;

@Service
public class ProjectService {

    private CommandGateway commandGateway;

    private ProjectRepository projectRepository;

    private TeamRepository teamRepository;

    private TenantService tenantService;

    public ProjectService(CommandGateway commandGateway,
            ProjectRepository projectRepository,
            TeamRepository teamRepository,
            TenantService tenantService) {
        this.commandGateway = commandGateway;
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
        this.tenantService = tenantService;
    }

    public String newProject(String name, String description,
            String createdBy, String teamId, String tenantId) {
        ProjectEntity existingProject = this.findByName(name);
        if (existingProject != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested project name {0} is not available.",
                    name));
        }

        TeamEntity team = findTeamById(teamId);
        Set<String> allMemberAndOwnerIds = getAllMemberAndOwnerIds(
                Collections.singletonList(team));

        if (tenantId == null) {
            TenantEntity tenantEntity = tenantService.findByName("Default");
            tenantId = tenantEntity.getTenantId();
        }
        else {
            TenantEntity tenantEntity = tenantService.findByTenantId(tenantId);
            if (tenantEntity == null) {
                throw new InvalidRequestException(MessageFormat.format(
                        "Supplied tenantId {0} does not exist.", tenantId));
            }
        }

        return commandGateway.sendAndWait(
                new NewProject(
                        UUID.randomUUID().toString(),
                        name,
                        description,
                        new TeamMemberId(createdBy),
                        new TeamId(teamId),
                        new TenantId(tenantId),
                        allMemberAndOwnerIds),
                1000,
                TimeUnit.SECONDS);
    }

    public String requestBitbucketProject(String projectId, String name,
            String projectKey, String description, String requestedBy) {
        Set<TeamEntity> projectAssociatedTeams = findTeamsByProjectId(
                projectId);
        Set<String> allMemberAndOwnerIds = getAllMemberAndOwnerIds(
                projectAssociatedTeams);
        return commandGateway.sendAndWait(
                new RequestBitbucketProject(
                        projectId,
                        BitbucketProject.builder()
                                .name(name)
                                .description(description)
                                .key(projectKey)
                                .build(),
                        new TeamMemberId(requestedBy),
                        allMemberAndOwnerIds),
                1000,
                TimeUnit.SECONDS);
    }

    public String confirmBitbucketProjectCreated(String projectId,
            String bitbucketProjectId, String url) {
        return commandGateway.sendAndWait(
                new AddBitbucketRepository(
                        projectId,
                        BitbucketProject.builder()
                                .id(bitbucketProjectId)
                                .url(url)
                                .build()),
                1000,
                TimeUnit.SECONDS);
    }

    public String linkExistingBitbucketProject(String projectId,
            String bitbucketProjectId,
            String bitbucketProjectName,
            String projectKey,
            String description,
            String url,
            String requestedBy) {
        Set<TeamEntity> projectAssociatedTeams = findTeamsByProjectId(
                projectId);
        Set<String> allMemberAndOwnerIds = getAllMemberAndOwnerIds(
                projectAssociatedTeams);

        BitbucketProject bitbucketProject = new BitbucketProject(
                bitbucketProjectId,
                projectKey,
                bitbucketProjectName,
                description,
                url);

        return commandGateway.sendAndWait(new LinkBitbucketProject(
                projectId,
                bitbucketProject,
                new TeamMemberId(requestedBy),
                allMemberAndOwnerIds));
    }

    public String newProjectEnvironment(String projectId, String requestedBy) {
        Set<TeamEntity> projectAssociatedTeams = findTeamsByProjectId(
                projectId);
        Set<String> allMemberAndOwnerIds = getAllMemberAndOwnerIds(
                projectAssociatedTeams);
        return commandGateway.sendAndWait(
                new NewProjectEnvironment(projectId,
                        new TeamMemberId(requestedBy),
                        allMemberAndOwnerIds),
                1,
                TimeUnit.SECONDS);
    }

    public String linkProjectToTeams(String projectId, String requestedBy,
            List<TeamResource> teamsToLink) {
        Set<TeamEntity> projectAssociatedTeams = findTeamsByProjectId(
                projectId);
        Set<String> allMemberAndOwnerIds = getAllMemberAndOwnerIds(
                projectAssociatedTeams);

        Set<TeamId> teamIdsToLink = new HashSet<>();

        for (TeamResource team : teamsToLink) {
            teamIdsToLink.add(new TeamId(team.getTeamId()));
        }

        return commandGateway.sendAndWait(
                new LinkTeamsToProject(projectId,
                        new TeamMemberId(requestedBy),
                        teamIdsToLink,
                        allMemberAndOwnerIds),
                1,
                TimeUnit.SECONDS);
    }

    @Transactional(readOnly = true)
    public ProjectEntity findByProjectId(String projectId) {
        return projectRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<ProjectEntity> findAll() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ProjectEntity findByName(String name) {
        return projectRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<ProjectEntity> findByTeamName(String teamName) {
        return projectRepository.findByTeams_Name(teamName);
    }

    @Transactional(readOnly = true)
    public TeamEntity findTeamById(String teamId) {
        return teamRepository.findByTeamId(teamId);
    }

    @Transactional(readOnly = true)
    public Set<TeamEntity> findTeamsByProjectId(String projectId) {
        return projectRepository.findByProjectId(projectId).getTeams();
    }

    @Transactional
    public void deleteProjectById(String projectId) {
        projectRepository.deleteByProjectId(projectId);
    }

    private Set<String> getAllMemberAndOwnerIds(Collection<TeamEntity> teams) {
        Set<String> teamMemberIds = new HashSet<>();
        for (TeamEntity team : teams) {
            for (TeamMemberEntity member : team.getMembers()) {
                teamMemberIds.add(member.getMemberId());
            }
            for (TeamMemberEntity owner : team.getOwners()) {
                teamMemberIds.add(owner.getMemberId());
            }
        }
        return teamMemberIds;
    }

}
