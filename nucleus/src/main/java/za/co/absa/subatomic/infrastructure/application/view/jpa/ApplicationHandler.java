package za.co.absa.subatomic.infrastructure.application.view.jpa;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.application.ApplicationCreated;
import za.co.absa.subatomic.domain.application.ApplicationDeleted;
import za.co.absa.subatomic.domain.application.BitbucketGitRepository;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;

@Component
public class ApplicationHandler {

    private ApplicationRepository applicationRepository;

    private TeamMemberRepository teamMemberRepository;

    private ProjectRepository projectRepository;

    public ApplicationHandler(ApplicationRepository applicationRepository,
            TeamMemberRepository teamMemberRepository,
            ProjectRepository projectRepository) {
        this.applicationRepository = applicationRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.projectRepository = projectRepository;
    }

    @EventHandler
    @Transactional
    void on(ApplicationCreated event) {
        ProjectEntity projectEntity = projectRepository
                .findByProjectId(event.getProjectId().getProjectId());

        TeamMemberEntity createdBy = teamMemberRepository
                .findByMemberId(event.getCreatedBy().getTeamMemberId());

        BitbucketGitRepository bitbucketRepository = event
                .getBitbucketRepository();

        ApplicationEntity applicationEntity = ApplicationEntity.builder()
                .applicationId(event.getApplicationId())
                .name(event.getName())
                .description(event.getDescription())
                .applicationType(event.getApplicationType())
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
    }

    @EventHandler
    @Transactional
    void on(ApplicationDeleted event) {
        applicationRepository.deleteByApplicationId(event.getApplicationId());
    }

}
