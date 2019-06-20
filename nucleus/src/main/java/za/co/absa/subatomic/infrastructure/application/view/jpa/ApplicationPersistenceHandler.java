package za.co.absa.subatomic.infrastructure.application.view.jpa;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.adapter.application.rest.BitbucketRepository;
import za.co.absa.subatomic.domain.application.Application;
import za.co.absa.subatomic.domain.application.ApplicationType;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@Component
public class ApplicationPersistenceHandler {

    private ProjectRepository projectRepository;

    private TeamMemberRepository teamMemberRepository;

    private ApplicationRepository applicationRepository;

    public ApplicationPersistenceHandler(ProjectRepository projectRepository,
            TeamMemberRepository teamMemberRepository,
            ApplicationRepository applicationRepository) {
        this.projectRepository = projectRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public ApplicationEntity createApplication(Application application) {
        ProjectEntity projectEntity = projectRepository
                .findByProjectId(application.getProjectId());

        TeamMemberEntity createdBy = teamMemberRepository
                .findByMemberId(application.getCreatedBy().getMemberId());

        BitbucketRepository bitbucketRepository = application
                .getBitbucketRepository();

        ApplicationEntity applicationEntity = ApplicationEntity.builder()
                .applicationId(UUID.randomUUID().toString())
                .name(application.getName())
                .description(application.getDescription())
                .applicationType(application.getApplicationType())
                .project(projectEntity)
                .createdBy(createdBy)
                .bitbucketRepository(new BitbucketRepositoryEmbedded(
                        bitbucketRepository.getBitbucketId(),
                        bitbucketRepository.getSlug(),
                        bitbucketRepository.getName(),
                        bitbucketRepository.getRepoUrl(),
                        bitbucketRepository.getRemoteUrl()))
                .build();

        applicationEntity = applicationRepository.save(applicationEntity);

        projectEntity.getApplications().add(applicationEntity);

        projectRepository.save(projectEntity);

        return applicationEntity;
    }

    @Transactional
    void deleteApplication(String applicationId) {
        applicationRepository.deleteByApplicationId(applicationId);
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
    public List<ApplicationEntity> findByName(String name) {
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
        return applicationRepository.findByNameAndProjectProjectId(name,
                projectId);
    }

    @Transactional(readOnly = true)
    public List<ApplicationEntity> findByProjectId(String projectId) {
        return applicationRepository.findByProjectProjectId(
                projectId);
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
}
