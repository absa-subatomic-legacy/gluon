package za.co.absa.subatomic.application.project;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import za.co.absa.subatomic.adapter.project.rest.TeamResource;
import za.co.absa.subatomic.application.member.TeamMemberService;
import za.co.absa.subatomic.application.team.TeamService;
import za.co.absa.subatomic.application.tenant.TenantService;
import za.co.absa.subatomic.domain.exception.ApplicationAuthorisationException;
import za.co.absa.subatomic.domain.exception.DuplicateRequestException;
import za.co.absa.subatomic.domain.exception.InvalidRequestException;
import za.co.absa.subatomic.domain.project.BitbucketProject;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.project.ProjectAutomationHandler;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectPersistenceHandler;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;

@Service
public class ProjectService {

    private TeamMemberService teamMemberService;

    private TeamService teamService;

    private TenantService tenantService;

    private ProjectPersistenceHandler projectPersistenceHandler;

    private ProjectAutomationHandler projectAutomationHandler;

    public ProjectService(
            TeamMemberService teamMemberService,
            TeamService teamService,
            TenantService tenantService,
            ProjectPersistenceHandler projectPersistenceHandler,
            ProjectAutomationHandler projectAutomationHandler) {
        this.teamMemberService = teamMemberService;
        this.teamService = teamService;
        this.tenantService = tenantService;
        this.projectPersistenceHandler = projectPersistenceHandler;
        this.projectAutomationHandler = projectAutomationHandler;
    }

    public String newProject(String name, String description,
            String createdBy, String teamId, String tenantId) {
        ProjectEntity existingProject = this.projectPersistenceHandler
                .findByName(name);
        if (existingProject != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested project name {0} is not available.",
                    name));
        }

        TeamEntity owningTeamEntity = this.teamService.findByTeamId(teamId);
        TenantEntity owningTenantEntity;
        if (tenantId == null) {
            owningTenantEntity = tenantService.findByName("Default");
        }
        else {
            owningTenantEntity = tenantService.findByTenantId(tenantId);
            if (owningTenantEntity == null) {
                throw new InvalidRequestException(MessageFormat.format(
                        "Supplied tenantId {0} does not exist.", tenantId));
            }
        }
        TeamMemberEntity createdByEntity = this.teamMemberService
                .findByTeamMemberId(createdBy);

        this.teamService.assertMemberBelongsToTeam(createdByEntity,
                owningTeamEntity);

        ProjectEntity newProject = this.projectPersistenceHandler.createProject(
                name, description,
                createdByEntity, owningTeamEntity, owningTenantEntity);

        this.projectAutomationHandler.projectCreated(newProject,
                owningTeamEntity, createdByEntity, owningTenantEntity);

        return newProject.getProjectId();
    }

    public String linkExistingBitbucketProject(String projectId,
            String bitbucketProjectId,
            String bitbucketProjectName,
            String projectKey,
            String description,
            String url,
            String actionedBy) {
        assertMemberBelongsToAnAssociatedTeam(projectId, actionedBy);

        BitbucketProject bitbucketProject = new BitbucketProject(
                bitbucketProjectId,
                projectKey,
                bitbucketProjectName,
                description,
                url);

        TeamMemberEntity actionedByEntity = teamMemberService
                .findByTeamMemberId(actionedBy);

        ProjectEntity projectEntity = this.projectPersistenceHandler
                .linkBitbucketProject(projectId, bitbucketProject,
                        actionedByEntity);

        this.projectAutomationHandler.bitbucketProjectLinkedToProject(
                projectEntity, bitbucketProject, actionedByEntity);

        return projectEntity.getProjectId();
    }

    public void newProjectEnvironment(String projectId, String requestedBy) {

        assertMemberBelongsToAnAssociatedTeam(projectId, requestedBy);

        ProjectEntity projectEntity = this.projectPersistenceHandler
                .findByProjectId(projectId);

        TeamMemberEntity teamMemberEntity = this.teamMemberService
                .findByTeamMemberId(requestedBy);

        this.projectAutomationHandler.requestProjectEnvironment(projectEntity,
                teamMemberEntity);
    }

    public String linkProjectToTeams(String projectId, String actionedBy,
            List<TeamResource> teamsToLink) {
        TeamMemberEntity actionedByEntity = teamMemberService
                .findByTeamMemberId(actionedBy);

        assertMemberBelongsToAnAssociatedTeam(projectId, actionedByEntity);

        List<TeamEntity> teamEntitiesToLink = new ArrayList<>();

        for (TeamResource team : teamsToLink) {
            teamEntitiesToLink.add(teamService.findByTeamId(team.getTeamId()));
        }

        ProjectEntity projectEntity = this.projectPersistenceHandler
                .linkTeamsToProject(projectId,
                        teamEntitiesToLink);

        this.projectAutomationHandler.teamsLinkedToProject(projectEntity,
                actionedByEntity, teamEntitiesToLink);

        return projectEntity.getProjectId();
    }

    public void deleteProject(String projectId) {
        this.projectPersistenceHandler.deleteProject(projectId);
    }

    private void assertMemberBelongsToAnAssociatedTeam(String projectId,
            String memberId) {
        TeamMemberEntity memberEntity = teamMemberService
                .findByTeamMemberId(memberId);
        assertMemberBelongsToAnAssociatedTeam(projectId, memberEntity);
    }

    private void assertMemberBelongsToAnAssociatedTeam(String projectId,
            TeamMemberEntity memberEntity) {
        Collection<TeamEntity> projectAssociatedTeams = this.projectPersistenceHandler
                .findTeamsAssociatedToProject(
                        projectId);
        if (!teamService.memberBelongsToAnyTeam(memberEntity,
                projectAssociatedTeams)) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "TeamMember with id {0} is not a member of any team associated to project with id {1}.",
                    memberEntity.getMemberId(),
                    projectId));
        }
    }

    public ProjectPersistenceHandler getProjectPersistenceHandler() {
        return this.projectPersistenceHandler;
    }
}
