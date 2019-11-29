package za.co.absa.subatomic.application.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.absa.subatomic.application.team.TeamAssertions;
import za.co.absa.subatomic.domain.application.Application;
import za.co.absa.subatomic.domain.exception.DuplicateRequestException;
import za.co.absa.subatomic.infrastructure.application.ApplicationAutomationHandler;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationEntity;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationPersistenceHandler;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationRepository;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberPersistenceHandler;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectPersistenceHandler;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

@Service
public class ApplicationService {

    private ApplicationRepository applicationRepository;

    private ProjectRepository projectRepository;

    private ApplicationPersistenceHandler applicationPersistenceHandler;

    private ProjectPersistenceHandler projectPersistenceHandler;

    private TeamMemberPersistenceHandler teamMemberPersistenceHandler;

    private ApplicationAutomationHandler applicationAutomationHandler;

    private TeamAssertions teamAssertions = new TeamAssertions();

    public ApplicationService(
            ApplicationRepository applicationRepository,
            ProjectRepository projectRepository,
            ApplicationPersistenceHandler applicationPersistenceHandler,
            ProjectPersistenceHandler projectPersistenceHandler,
            TeamMemberPersistenceHandler teamMemberPersistenceHandler,
            ApplicationAutomationHandler applicationAutomationHandler) {
        this.applicationRepository = applicationRepository;
        this.projectRepository = projectRepository;
        this.applicationPersistenceHandler = applicationPersistenceHandler;
        this.projectPersistenceHandler = projectPersistenceHandler;
        this.teamMemberPersistenceHandler = teamMemberPersistenceHandler;
        this.applicationAutomationHandler = applicationAutomationHandler;
    }

    public ApplicationEntity newApplication(
            Application newApplication,
            boolean configurationRequested) {
        ApplicationEntity existingApplication = this.applicationPersistenceHandler
                .findByNameAndProjectId(newApplication.getName(),
                        newApplication.getProjectId());

        if (existingApplication != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Application with name {0} already exists in project with id {1}.",
                    newApplication.getName(), newApplication.getProjectId()));
        }

        Collection<TeamEntity> teamsAssociatedToProject = this.projectPersistenceHandler
                .findTeamsAssociatedToProject(newApplication.getProjectId());

        TeamMemberEntity createdBy = this.teamMemberPersistenceHandler
                .findByTeamMemberId(
                        newApplication.getCreatedBy().getMemberId());

        teamAssertions.memberBelongsToAnyTeam(createdBy,
                teamsAssociatedToProject);

        ApplicationEntity applicationEntity = this.applicationPersistenceHandler
                .createApplication(newApplication);

        this.applicationAutomationHandler.applicationCreated(applicationEntity,
                configurationRequested);

        return applicationEntity;
    }

    @Transactional
    public void setApplicationJenkinsfolder(String applicationId, String jenkinsFolder) {
        this.applicationPersistenceHandler.setApplicationJenkinsfolder(applicationId, jenkinsFolder);
    }

    @Transactional
    public void deleteApplication(String applicationId) {
        ApplicationEntity applicationEntity = applicationRepository
                .findByApplicationId(applicationId);
        ProjectEntity projectEntity = applicationEntity.getProject();
        projectEntity.getApplications().remove(applicationEntity);
        projectRepository.save(projectEntity);
        applicationRepository.deleteByApplicationId(applicationId);
    }

    @Transactional
    public ApplicationEntity findByApplicationId(String applicationId) {
        return this.applicationPersistenceHandler.findByApplictionId(applicationId);
    }

    @Transactional
    public ApplicationEntity findByNameAndProjectId(String name, String projectId) {
        return this.applicationPersistenceHandler.findByNameAndProjectId(name, projectId);
    }

    @Transactional
    public ApplicationEntity findByNameAndProjectName(String name, String projectName) {
        return this.applicationPersistenceHandler.findByNameAndProjectName(name, projectName);
    }

    @Transactional
    public List<ApplicationEntity> findByProjectName(String projectName) {
        return this.applicationPersistenceHandler.findByProjectName(projectName);
    }

    @Transactional
    public List<ApplicationEntity> findByApplicationType(String applicationType) {
        return this.applicationPersistenceHandler.findByApplicationType(applicationType);
    }

    @Transactional
    public List<ApplicationEntity> findByProjectId(String projectId) {
        return this.applicationPersistenceHandler.findByProjectId(projectId);
    }

    @Transactional
    public List<ApplicationEntity> findAll() {
        return this.applicationPersistenceHandler.findAll();
    }
}
