package za.co.absa.subatomic.application.project;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import za.co.absa.subatomic.domain.project.AddBitbucketRepository;
import za.co.absa.subatomic.domain.project.BitbucketProject;
import za.co.absa.subatomic.domain.project.NewProject;
import za.co.absa.subatomic.domain.project.NewProjectEnvironment;
import za.co.absa.subatomic.domain.project.RequestBitbucketProject;
import za.co.absa.subatomic.domain.project.TeamId;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private CommandGateway commandGateway;

    private ProjectRepository projectRepository;

    public ProjectService(CommandGateway commandGateway,
            ProjectRepository projectRepository) {
        this.commandGateway = commandGateway;
        this.projectRepository = projectRepository;
    }

    public String newProject(String name, String description,
            String createdBy, String team) {
        return commandGateway.sendAndWait(
                new NewProject(
                        UUID.randomUUID().toString(),
                        name,
                        description,
                        new TeamMemberId(createdBy),
                        new TeamId(team)),
                1000,
                TimeUnit.SECONDS);
    }

    public String requestBitbucketProject(String projectId, String name,
            String description, String requestedBy) {
        return commandGateway.sendAndWait(
                new RequestBitbucketProject(
                        projectId,
                        BitbucketProject.builder()
                                .name(name)
                                .description(description)
                                .build(),
                        new TeamMemberId(requestedBy)),
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
        return commandGateway.sendAndWait(
                new NewProjectEnvironment(projectId,
                        new TeamMemberId(requestedBy)),
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

}
