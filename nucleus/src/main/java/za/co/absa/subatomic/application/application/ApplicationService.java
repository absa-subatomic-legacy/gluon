package za.co.absa.subatomic.application.application;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.application.ApplicationType;
import za.co.absa.subatomic.domain.application.BitbucketGitRepository;
import za.co.absa.subatomic.domain.application.NewApplication;
import za.co.absa.subatomic.domain.application.RequestApplicationEnvironment;
import za.co.absa.subatomic.domain.exception.DuplicateRequestException;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationEntity;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationRepository;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@Service
public class ApplicationService {

    private CommandGateway commandGateway;

    private ApplicationRepository applicationRepository;

    private ProjectRepository projectRepository;

    public ApplicationService(CommandGateway commandGateway,
            ApplicationRepository applicationRepository,
            ProjectRepository projectRepository) {
        this.commandGateway = commandGateway;
        this.applicationRepository = applicationRepository;
        this.projectRepository = projectRepository;
    }

    public String newApplication(String name, String description,
            String applicationType,
            String projectId, String requestedBy) {

        ApplicationEntity existingApplication = this
                .findByNameAndProjectName(name, projectId);
        if (existingApplication != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Application with name {0} already exists in project with id {1}.",
                    name, projectId));
        }

        Set<TeamEntity> teamsAssociatedWithProject = findTeamsByProjectId(
                projectId);
        Set<String> allMemberAndOwnerIds = getAllMemberAndOwnerIds(
                teamsAssociatedWithProject);
        return commandGateway.sendAndWait(
                new NewApplication(
                        UUID.randomUUID().toString(),
                        name,
                        description,
                        ApplicationType.valueOf(applicationType),
                        new ProjectId(projectId),
                        new TeamMemberId(requestedBy),
                        allMemberAndOwnerIds),
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

        Set<TeamEntity> teamsAssociatedWithProject = findTeamsByProjectId(
                projectId);
        Set<String> allMemberAndOwnerIds = getAllMemberAndOwnerIds(
                teamsAssociatedWithProject);
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
                        new TeamMemberId(requestedBy),
                        allMemberAndOwnerIds),
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
    public ApplicationEntity findByNameAndProjectName(String name,
            String projectName) {
        return applicationRepository.findByNameAndProjectName(name,
                projectName);
    }

    @Transactional(readOnly = true)
    public ApplicationEntity findByNameAndProjectId(String name,
            String projectId) {
        return applicationRepository.findByNameAndProjectId(name,
                Long.valueOf(projectId));
    }

    @Transactional(readOnly = true)
    public List<ApplicationEntity> findByApplicationType(
            String applicationType) {
        return applicationRepository.findByApplicationType(
                ApplicationType.valueOf(applicationType.toUpperCase()));
    }

    @Transactional(readOnly = true)
    public Set<TeamEntity> findTeamsByProjectId(String projectId) {
        return projectRepository.findByProjectId(projectId).getTeams();
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
