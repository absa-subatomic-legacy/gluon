package za.co.absa.subatomic.application.team;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.adapter.team.rest.MembershipRequestResource;
import za.co.absa.subatomic.application.member.TeamMemberService;
import za.co.absa.subatomic.domain.exception.DuplicateRequestException;
import za.co.absa.subatomic.domain.exception.InvalidRequestException;
import za.co.absa.subatomic.domain.team.MembershipRequestStatus;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;
import za.co.absa.subatomic.infrastructure.team.TeamAutomationHandler;
import za.co.absa.subatomic.infrastructure.team.view.jpa.MembershipRequestEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.MembershipRequestRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamPersistenceHandler;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamRepository;

@Service
@Slf4j
public class TeamService {

    private TeamRepository teamRepository;

    private ProjectRepository projectRepository;

    private TeamMemberService teamMemberService;

    private TeamAutomationHandler automationHandler;

    private TeamPersistenceHandler persistenceHandler;

    private MembershipRequestRepository membershipRequestRepository;

    private TeamAssertions teamAssertions = new TeamAssertions();

    public TeamService(TeamRepository teamRepository,
            MembershipRequestRepository membershipRequestRepository,
            ProjectRepository projectRepository,
            TeamMemberService teamMemberService,
            TeamAutomationHandler automationHandler,
            TeamPersistenceHandler persistenceHandler) {
        this.teamRepository = teamRepository;
        this.membershipRequestRepository = membershipRequestRepository;
        this.projectRepository = projectRepository;
        this.teamMemberService = teamMemberService;
        this.automationHandler = automationHandler;
        this.persistenceHandler = persistenceHandler;
    }

    public TeamEntity newTeamFromSlack(String name, String description,
            String openShiftCloud,
            String createdBy,
            String teamChannel) {
        TeamEntity existingTeam = this.persistenceHandler.findByName(name);
        if (existingTeam != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested team name {0} is not available.",
                    name));
        }

        TeamEntity newTeam = this.persistenceHandler.createTeam(name,
                description, openShiftCloud, teamChannel, createdBy);

        this.automationHandler.createNewTeam(newTeam);

        return newTeam;
    }

    public void addTeamMembers(String teamId, String actionedBy,
            List<String> teamOwnerIds,
            List<String> teamMemberIds) {

        TeamEntity team = this.teamRepository.findByTeamId(teamId);

        TeamMemberEntity actionedByEntity = this.teamMemberService
                .getTeamMemberPersistenceHandler()
                .findByTeamMemberId(actionedBy);

        teamAssertions.assertMemberIsOwnerOfTeam(actionedByEntity, team);

        Optional<TeamMemberEntity> existingMember = team.getMembers().stream()
                .filter(memberEntity -> teamMemberIds
                        .contains(memberEntity.getMemberId()))
                .findFirst();
        Optional<TeamMemberEntity> existingOwner = team.getOwners().stream()
                .filter(memberEntity -> teamOwnerIds
                        .contains(memberEntity.getMemberId()))
                .findFirst();

        existingMember.ifPresent(teamMemberEntity -> {
            throw new InvalidRequestException(MessageFormat.format(
                    "Member {0} is already a member of team {1}",
                    teamMemberEntity.getMemberId(), teamId));
        });

        existingOwner.ifPresent(teamMemberEntity -> {
            throw new InvalidRequestException(MessageFormat.format(
                    "Member {0} is already an owner of team {1}",
                    teamMemberEntity.getMemberId(), teamId));
        });

        this.persistenceHandler.addTeamMembers(teamId, teamOwnerIds,
                teamMemberIds);

        List<TeamMemberEntity> newTeamMembers = this.teamMemberService
                .getTeamMemberPersistenceHandler()
                .findAllTeamMembersById(teamMemberIds);
        List<TeamMemberEntity> newOwners = this.teamMemberService
                .getTeamMemberPersistenceHandler()
                .findAllTeamMembersById(teamOwnerIds);

        this.automationHandler.teamMembersAdded(team, newOwners,
                newTeamMembers);

    }

    public void removeTeamMember(String teamId, String memberId,
            String requestedById) {

        TeamEntity team = this.persistenceHandler.findByTeamId(teamId);
        TeamMemberEntity actionedByEntity = this.teamMemberService
                .getTeamMemberPersistenceHandler()
                .findByTeamMemberId(requestedById);
        TeamMemberEntity member = this.teamMemberService
                .getTeamMemberPersistenceHandler()
                .findByTeamMemberId(memberId);

        teamAssertions.assertMemberIsOwnerOfTeam(actionedByEntity, team);

        this.persistenceHandler.removeTeamMember(teamId, memberId);

        this.automationHandler.teamMemberRemoved(team, member,
                actionedByEntity);
    }

    public TeamEntity addSlackIdentity(String teamId, String teamChannel) {
        return this.persistenceHandler.addSlackIdentity(teamId,
                teamChannel);
    }

    public void newDevOpsEnvironment(String teamId, String requestedById) {
        TeamEntity team = this.persistenceHandler.findByTeamId(teamId);
        TeamMemberEntity requestedBy = this.teamMemberService
                .getTeamMemberPersistenceHandler()
                .findByTeamMemberId(requestedById);

        teamAssertions.assertMemberBelongsToTeam(requestedBy, team);

        this.automationHandler.devOpsEnvironmentRequested(team, requestedBy);
    }

    public void updateMembershipRequest(String teamId,
            MembershipRequestResource membershipRequest) {
        TeamMemberEntity approver = this.teamMemberService
                .getTeamMemberPersistenceHandler().findByTeamMemberId(
                        membershipRequest.getApprovedBy().getMemberId());
        TeamEntity team = this.persistenceHandler.findByTeamId(teamId);
        teamAssertions.assertMemberIsOwnerOfTeam(approver, team);

        MembershipRequestEntity membershipRequestEntity = this.membershipRequestRepository
                .findByMembershipRequestId(
                        membershipRequest.getMembershipRequestId());

        if (membershipRequestEntity
                .getRequestStatus() != MembershipRequestStatus.OPEN) {
            throw new InvalidRequestException(MessageFormat.format(
                    "This membership request to team {0} has already been closed.",
                    teamId));
        }

        membershipRequestEntity = this.persistenceHandler
                .closeMembershipRequest(
                        membershipRequest.getMembershipRequestId(),
                        membershipRequest.getRequestStatus(),
                        membershipRequest.getApprovedBy().getMemberId());

        if (membershipRequestEntity
                .getRequestStatus() == MembershipRequestStatus.APPROVED) {
            TeamMemberEntity newMemberEntity = this.teamMemberService
                    .getTeamMemberPersistenceHandler()
                    .findByTeamMemberId(membershipRequestEntity.getRequestedBy()
                            .getMemberId());
            this.addTeamMembers(teamId,
                    membershipRequestEntity.getApprovedBy().getMemberId(),
                    Collections.emptyList(),
                    Collections.singletonList(newMemberEntity.getMemberId()));
        }
    }

    public void newMembershipRequest(String teamId,
            String requestByMemberId) {
        TeamEntity teamEntity = this.persistenceHandler.findByTeamId(teamId);
        TeamMemberEntity requestedBy = this.teamMemberService
                .getTeamMemberPersistenceHandler()
                .findByTeamMemberId(requestByMemberId);

        teamAssertions.assertMemberDoesNotBelongToTeam(requestedBy, teamEntity);

        for (MembershipRequestEntity request : teamEntity
                .getMembershipRequests()) {
            if (request.getRequestStatus() == MembershipRequestStatus.OPEN &&
                    request.getRequestedBy().getMemberId().equals(
                            requestByMemberId)) {
                throw new InvalidRequestException(MessageFormat.format(
                        "An open membership request to team {0} already exists for requesting user {1}",
                        teamId, requestByMemberId));
            }
        }

        MembershipRequestEntity membershipRequest = this.persistenceHandler
                .createMembershipRequest(teamId, requestByMemberId);

        this.automationHandler.membershipRequestCreated(teamEntity,
                membershipRequest);
    }

    @Transactional(readOnly = true)
    public Set<TeamEntity> findTeamsAssociatedToProject(String projectId) {
        ProjectEntity projectEntity = projectRepository
                .findByProjectId(projectId);
        Set<TeamEntity> associatedTeams = new HashSet<>();
        associatedTeams.add(projectEntity.getOwningTeam());
        associatedTeams.addAll(projectEntity.getTeams());
        return associatedTeams;
    }

    public TeamPersistenceHandler getPersistenceHandler() {
        return persistenceHandler;
    }
}
