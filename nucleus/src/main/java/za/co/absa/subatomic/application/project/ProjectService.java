package za.co.absa.subatomic.application.project;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.project.AddBitbucketRepository;
import za.co.absa.subatomic.domain.project.BitbucketProject;
import za.co.absa.subatomic.domain.project.NewProject;
import za.co.absa.subatomic.domain.project.NewProjectEnvironment;
import za.co.absa.subatomic.domain.project.RequestBitbucketProject;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamRepository;

@Service
public class ProjectService {

    private CommandGateway commandGateway;

    private ProjectRepository projectRepository;

    private TeamRepository teamRepository;

    public ProjectService(CommandGateway commandGateway,
            ProjectRepository projectRepository,
            TeamRepository teamRepository) {
        this.commandGateway = commandGateway;
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
    }

    public String newProject(String name, String description,
            String createdBy, String teamId) {
        TeamEntity team = findTeamById(teamId);
        return commandGateway.sendAndWait(
                new NewProject(
                        UUID.randomUUID().toString(),
                        name,
                        description,
                        new TeamMemberId(createdBy),
                        team),
                1000,
                TimeUnit.SECONDS);
    }

    public String requestBitbucketProject(String projectId, String name,
            String description, String requestedBy) {
        Set<TeamEntity> projectAssociatedTeams = findTeamsByProjectId(
                projectId);
        return commandGateway.sendAndWait(
                new RequestBitbucketProject(
                        projectId,
                        BitbucketProject.builder()
                                .name(name)
                                .description(description)
                                .build(),
                        new TeamMemberId(requestedBy),
                        projectAssociatedTeams),
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

    public String newProjectEnvironment(String projectId, String requestedBy) {
        Set<TeamEntity> projectAssociatedTeams = findTeamsByProjectId(
                projectId);
        return commandGateway.sendAndWait(
                new NewProjectEnvironment(projectId,
                        new TeamMemberId(requestedBy),
                        projectAssociatedTeams),
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
}
