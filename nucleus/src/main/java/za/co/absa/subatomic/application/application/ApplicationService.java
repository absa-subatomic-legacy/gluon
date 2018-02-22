package za.co.absa.subatomic.application.application;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import za.co.absa.subatomic.domain.application.ApplicationType;
import za.co.absa.subatomic.domain.application.BitbucketGitRepository;
import za.co.absa.subatomic.domain.application.NewApplication;
import za.co.absa.subatomic.domain.application.RequestApplicationEnvironment;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationEntity;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

    private CommandGateway commandGateway;

    private ApplicationRepository applicationRepository;

    public ApplicationService(CommandGateway commandGateway,
            ApplicationRepository applicationRepository) {
        this.commandGateway = commandGateway;
        this.applicationRepository = applicationRepository;
    }

    public String newApplication(String name, String description,
            String applicationType,
            String projectId, String requestedBy) {
        return commandGateway.sendAndWait(
                new NewApplication(
                        UUID.randomUUID().toString(),
                        name,
                        description,
                        ApplicationType.valueOf(applicationType),
                        new ProjectId(projectId),
                        new TeamMemberId(requestedBy)),
                1000,
                TimeUnit.SECONDS);
    }

    public String requestApplicationEnvironment(String applicationId,
            String bitbucketRepoId,
            String bitbucketRepoSlug,
            String bitbucketRepoName,
            String bitbucketRepoUrl,
            String bitbucketRepoRemoteUrl,
            String projectId,
            String requestedBy) {
        return commandGateway.sendAndWait(
                new RequestApplicationEnvironment(
                        applicationId,
                        bitbucketRepoName,
                        BitbucketGitRepository.builder()
                                .bitbucketId(bitbucketRepoId)
                                .slug(bitbucketRepoSlug)
                                .name(bitbucketRepoName)
                                .repoUrl(bitbucketRepoUrl)
                                .remoteUrl(bitbucketRepoRemoteUrl)
                                .build(),
                        new ProjectId(projectId),
                        new TeamMemberId(requestedBy)),
                1000,
                TimeUnit.SECONDS);
    }

    @Transactional(readOnly = true)
    public ApplicationEntity findByApplictionId(String applicationId) {
        return applicationRepository.findByApplicationId(applicationId);
    }

    @Transactional(readOnly = true)
    public List<ApplicationEntity> findAll() {
        return applicationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ApplicationEntity findByName(String name) {
        return applicationRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<ApplicationEntity> findByProjectName(String projectName) {
        return applicationRepository.findByProjectName(projectName);
    }

    @Transactional(readOnly = true)
    public List<ApplicationEntity> findByApplicationType(
            String applicationType) {
        return applicationRepository.findByApplicationType(
                ApplicationType.valueOf(applicationType.toUpperCase()));
    }
}
