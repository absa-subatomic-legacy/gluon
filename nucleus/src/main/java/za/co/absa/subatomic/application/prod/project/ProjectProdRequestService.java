package za.co.absa.subatomic.application.prod.project;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.application.member.TeamMemberService;
import za.co.absa.subatomic.application.project.ProjectService;
import za.co.absa.subatomic.application.team.TeamService;
import za.co.absa.subatomic.domain.exception.ApplicationAuthorisationException;
import za.co.absa.subatomic.domain.exception.InvalidRequestException;
import za.co.absa.subatomic.domain.prod.project.ProjectProductionRequestStatus;
import za.co.absa.subatomic.infrastructure.configuration.GluonProperties;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.prod.project.ProjectProdRequestAutomationHandler;
import za.co.absa.subatomic.infrastructure.prod.project.view.jpa.ProjectProdRequestEntity;
import za.co.absa.subatomic.infrastructure.prod.project.view.jpa.ProjectProdRequestRepository;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@Service
public class ProjectProdRequestService {

    private ProjectService projectService;

    private TeamMemberService teamMemberService;

    private TeamService teamService;

    private ProjectProdRequestRepository projectProdRequestRepository;

    private ProjectProdRequestAutomationHandler automationHandler;

    private GluonProperties gluonProperties;

    public ProjectProdRequestService(
            ProjectService projectService,
            TeamMemberService teamMemberService,
            TeamService teamService,
            ProjectProdRequestRepository projectProdRequestRepository,
            ProjectProdRequestAutomationHandler automationHandler,
            GluonProperties gluonProperties) {
        this.projectService = projectService;
        this.teamMemberService = teamMemberService;
        this.teamService = teamService;
        this.projectProdRequestRepository = projectProdRequestRepository;
        this.automationHandler = automationHandler;
        this.gluonProperties = gluonProperties;
    }

    public ProjectProdRequestEntity createProjectProdRequest(String projectId,
            String actionedByMemberId) {
        ProjectEntity projectEntity = this.projectService
                .findByProjectId(projectId);
        TeamMemberEntity actionedBy = this.teamMemberService
                .findByTeamMemberId(actionedByMemberId);

        Set<TeamEntity> memberAssociatedTeams = this.teamService
                .findByMemberOrOwnerMemberId(actionedByMemberId);

        throwErrorIfMemberIsNotAMemberOfAssociatedTeam(actionedByMemberId,
                projectId, memberAssociatedTeams, projectEntity.getTeams());

        // Close and mark any open requests as rejected
        for (ProjectProdRequestEntity openProdRequest : this.projectProdRequestRepository
                .findByApprovalStatus(ProjectProductionRequestStatus.PENDING)) {
            openProdRequest.setRejectingMember(actionedBy);
            openProdRequest
                    .setApprovalStatus(ProjectProductionRequestStatus.REJECTED);
            openProdRequest.close();
            this.projectProdRequestRepository.save(openProdRequest);
            ProjectProdRequestEntity rejectedProdRequest = this
                    .addRejectionToProjectProdRequest(openProdRequest,
                            actionedBy);
            this.automationHandler
                    .projectProdRequestClosed(rejectedProdRequest);
        }

        ProjectProdRequestEntity projectProdRequestEntity = this
                .createProdProjectRequestEntity(projectEntity, actionedBy);

        if (projectProdRequestEntity != null) {
            this.automationHandler
                    .projectProdRequestCreated(projectProdRequestEntity);
        }

        return projectProdRequestEntity;
    }

    @Transactional
    protected ProjectProdRequestEntity createProdProjectRequestEntity(
            ProjectEntity projectEntity, TeamMemberEntity actionedBy) {
        ProjectProdRequestEntity projectProdRequestEntityInitial = ProjectProdRequestEntity
                .builder()
                .projectProdRequestId(UUID.randomUUID().toString())
                .project(projectEntity)
                .projectName(projectEntity.getName())
                .actionedBy(actionedBy)
                .approvalStatus(ProjectProductionRequestStatus.PENDING)
                .build();
        return projectProdRequestRepository
                .save(projectProdRequestEntityInitial);
    }

    public ProjectProdRequestEntity addProjectProdRequestApproval(
            String projectProdRequestId, String approvingMemberId) {
        ProjectProdRequestEntity projectProdRequest = projectProdRequestRepository
                .findByProjectProdRequestId(projectProdRequestId);
        TeamMemberEntity approvingMember = this.teamMemberService
                .findByTeamMemberId(approvingMemberId);
        Set<TeamEntity> memberAssociatedTeams = this.teamService
                .findByMemberOrOwnerMemberId(approvingMemberId);

        throwErrorIfMemberIsNotAMemberOfAssociatedTeam(approvingMemberId,
                projectProdRequest.getProject().getProjectId(),
                memberAssociatedTeams,
                projectProdRequest.getProject().getTeams());

        throwErrorIfProjectProdRequestIsClosed(projectProdRequest);

        throwErrorIfMemberIsHasAlreadyAuthorizedRequest(approvingMemberId,
                projectProdRequest);

        ProjectProdRequestEntity projectProdRequestEntity = this
                .addApprovalToProjectProdRequest(projectProdRequest,
                        approvingMember);

        if (projectProdRequestEntity != null && projectProdRequestEntity
                .getApprovalStatus() == ProjectProductionRequestStatus.APPROVED) {
            this.automationHandler
                    .projectProdRequestClosed(projectProdRequestEntity);
        }

        return projectProdRequestEntity;
    }

    @Transactional
    protected ProjectProdRequestEntity addApprovalToProjectProdRequest(
            ProjectProdRequestEntity projectProdRequest,
            TeamMemberEntity approvingMember) {
        projectProdRequest.getAuthorizingMembers().add(approvingMember);

        if (projectProdRequest.getAuthorizingMembers().size() >= this
                .getRequiredProdApprovalCount(
                        projectProdRequest.getProject().getOwningTeam())) {
            projectProdRequest
                    .setApprovalStatus(ProjectProductionRequestStatus.APPROVED);
            projectProdRequest.close();
        }

        return projectProdRequestRepository
                .save(projectProdRequest);
    }

    public ProjectProdRequestEntity rejectProjectProdRequest(
            String projectProdRequestId, String rejectingMemberId) {
        ProjectProdRequestEntity projectProdRequest = projectProdRequestRepository
                .findByProjectProdRequestId(projectProdRequestId);
        TeamMemberEntity rejectingMember = this.teamMemberService
                .findByTeamMemberId(rejectingMemberId);
        Set<TeamEntity> memberAssociatedTeams = this.teamService
                .findByMemberOrOwnerMemberId(rejectingMemberId);

        throwErrorIfMemberIsNotAMemberOfAssociatedTeam(rejectingMemberId,
                projectProdRequest.getProject().getProjectId(),
                memberAssociatedTeams,
                projectProdRequest.getProject().getTeams());

        throwErrorIfProjectProdRequestIsClosed(projectProdRequest);

        throwErrorIfMemberIsHasAlreadyAuthorizedRequest(rejectingMemberId,
                projectProdRequest);

        ProjectProdRequestEntity projectProdRequestEntity = this
                .addRejectionToProjectProdRequest(projectProdRequest,
                        rejectingMember);
        if (projectProdRequestEntity != null) {
            this.automationHandler
                    .projectProdRequestClosed(projectProdRequestEntity);
        }

        return projectProdRequestEntity;
    }

    @Transactional
    protected ProjectProdRequestEntity addRejectionToProjectProdRequest(
            ProjectProdRequestEntity projectProdRequest,
            TeamMemberEntity rejectingMember) {
        projectProdRequest.setRejectingMember(rejectingMember);

        projectProdRequest
                .setApprovalStatus(ProjectProductionRequestStatus.REJECTED);
        projectProdRequest.close();

        return projectProdRequestRepository
                .save(projectProdRequest);
    }

    @Transactional(readOnly = true)
    public ProjectProdRequestEntity findByProjectProdRequestId(String id) {
        return this.projectProdRequestRepository.findByProjectProdRequestId(id);
    }

    @Transactional(readOnly = true)
    public List<ProjectProdRequestEntity> findAll() {
        return this.projectProdRequestRepository.findAll();
    }

    private void throwErrorIfMemberIsNotAMemberOfAssociatedTeam(String memberId,
            String projectId, Collection<TeamEntity> memberAssociatedTeams,
            Collection<TeamEntity> projectAssociatedTeams) {
        if (projectAssociatedTeams.stream()
                .noneMatch(memberAssociatedTeams::contains)) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "TeamMember with id {0} is not a member of any team associated to the project {1}.",
                    memberId,
                    projectId));
        }
    }

    private void throwErrorIfProjectProdRequestIsClosed(
            ProjectProdRequestEntity projectProdRequest) {
        ProjectProductionRequestStatus status = projectProdRequest
                .getApprovalStatus();
        if (status != ProjectProductionRequestStatus.PENDING) {
            throw new InvalidRequestException(MessageFormat.format(
                    "RequestApproval with id {0} is already closed with a status of {1}.",
                    projectProdRequest.getProjectProdRequestId(),
                    status));
        }
    }

    private void throwErrorIfMemberIsHasAlreadyAuthorizedRequest(
            String memberId, ProjectProdRequestEntity projectProdRequest) {
        if (projectProdRequest.getAuthorizingMembers().stream()
                .map(TeamMemberEntity::getMemberId)
                .anyMatch(memberId::equals)) {
            throw new InvalidRequestException(MessageFormat.format(
                    "The actioning member with id {0} has already approved this request.",
                    memberId));
        }
    }

    private int getRequiredProdApprovalCount(TeamEntity teamEntity) {
        int defaultProdApprovals = this.gluonProperties.getProject()
                .getDefaultProdApprovals();
        int totalMemberCount = teamEntity.getMembers().size()
                + teamEntity.getOwners().size();

        return Math.min(defaultProdApprovals, totalMemberCount);
    }
}
