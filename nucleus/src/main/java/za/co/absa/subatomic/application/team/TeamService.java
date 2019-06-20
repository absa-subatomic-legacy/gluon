package za.co.absa.subatomic.application.team;

import lombok.extern.slf4j.Slf4j;
import java.text.MessageFormat;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.adapter.metadata.rest.MetadataResource;
import za.co.absa.subatomic.adapter.team.rest.MembershipRequestResource;
import za.co.absa.subatomic.domain.exception.DuplicateRequestException;
import za.co.absa.subatomic.domain.exception.InvalidRequestException;
import za.co.absa.subatomic.domain.team.MembershipRequestStatus;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberPersistenceHandler;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectPersistenceHandler;
import za.co.absa.subatomic.infrastructure.team.TeamAutomationHandler;
import za.co.absa.subatomic.infrastructure.team.view.jpa.MembershipRequestEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.MembershipRequestRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamPersistenceHandler;

@Service
@Slf4j
public class TeamService {

    private TeamMemberPersistenceHandler teamMemberPersistenceHandler;

    private TeamAutomationHandler automationHandler;

    private TeamPersistenceHandler teamPersistenceHandler;

    private ProjectPersistenceHandler projectPersistenceHandler;

    private MembershipRequestRepository membershipRequestRepository;

    private TeamAssertions teamAssertions = new TeamAssertions();

    public TeamService(MembershipRequestRepository membershipRequestRepository,
                       TeamMemberPersistenceHandler teamMemberPersistenceHandler,
                       TeamAutomationHandler automationHandler,
                       TeamPersistenceHandler teamPersistenceHandler,
                       ProjectPersistenceHandler projectPersistenceHandler) {
        this.membershipRequestRepository = membershipRequestRepository;
        this.teamMemberPersistenceHandler = teamMemberPersistenceHandler;
        this.automationHandler = automationHandler;
        this.teamPersistenceHandler = teamPersistenceHandler;
        this.projectPersistenceHandler = projectPersistenceHandler;
    }

    public TeamEntity newTeamFromSlack(String name,
                                       String description,
                                       String openShiftCloud,
                                       String createdBy,
                                       List<MetadataResource> metadata,
                                       String teamChannel) {
        TeamEntity existingTeam = this.teamPersistenceHandler.findByName(name);
        if (existingTeam != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested team name {0} is not available.",
                    name));
        }

        TeamEntity newTeam = this.teamPersistenceHandler.createTeam(name,
                description, openShiftCloud, teamChannel, createdBy, metadata);

        this.automationHandler.createNewTeam(newTeam);

        return newTeam;
    }

    public void deleteTeam(String teamId) {
        TeamEntity teamEntity = this.teamPersistenceHandler.findByTeamId(teamId);
        List<ProjectEntity> projects = this.projectPersistenceHandler.findByTeamName(teamEntity.getName());
        for (ProjectEntity project : projects) {
            if (project.getOwningTeam().equals(teamEntity)) {
                projectPersistenceHandler.deleteProject(project.getProjectId());
            } else {
                projectPersistenceHandler.unlinkTeamsFromProject(project, Collections.singletonList(teamEntity));
            }
        }
        this.teamPersistenceHandler.deleteTeam(teamId);
    }

    public void setMetadata(String teamId,
                            List<MetadataResource> metadata) {
        TeamEntity existingTeam = this.teamPersistenceHandler.findByTeamId(teamId);

        if (existingTeam == null) {
            throw new InvalidRequestException(MessageFormat.format(
                    "Requested team with ID {0} does not exist", teamId
            ));
        }

        this.teamPersistenceHandler.setTeamMetadata(existingTeam, metadata);
    }

    public void updateMetadata(String teamId,
                               List<MetadataResource> metadata) {
        TeamEntity existingTeam = this.teamPersistenceHandler.findByTeamId(teamId);

        if (existingTeam == null) {
            throw new InvalidRequestException(MessageFormat.format(
                    "Requested team with ID {0} does not exist", teamId
            ));
        }

        this.teamPersistenceHandler.updateTeamMetadata(existingTeam, metadata);
    }


    public void addTeamMembers(String teamId, String actionedBy,
                               List<String> teamOwnerIds,
                               List<String> teamMemberIds) {

        TeamEntity team = this.teamPersistenceHandler.findByTeamId(teamId);

        TeamMemberEntity actionedByEntity = this.teamMemberPersistenceHandler
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

        this.teamPersistenceHandler.addTeamMembers(teamId, teamOwnerIds,
                teamMemberIds);

        List<TeamMemberEntity> newTeamMembers = this.teamMemberPersistenceHandler
                .findAllTeamMembersById(teamMemberIds);
        List<TeamMemberEntity> newOwners = this.teamMemberPersistenceHandler
                .findAllTeamMembersById(teamOwnerIds);

        this.automationHandler.teamMembersAdded(team, newOwners,
                newTeamMembers);

    }

    public void removeTeamMember(String teamId, String memberId,
                                 String requestedById) {

        TeamEntity team = this.teamPersistenceHandler.findByTeamId(teamId);
        TeamMemberEntity actionedByEntity = this.teamMemberPersistenceHandler
                .findByTeamMemberId(requestedById);
        TeamMemberEntity member = this.teamMemberPersistenceHandler
                .findByTeamMemberId(memberId);

        teamAssertions.assertMemberIsOwnerOfTeam(actionedByEntity, team);

        this.teamPersistenceHandler.removeTeamMember(teamId, memberId);

        this.automationHandler.teamMemberRemoved(team, member,
                actionedByEntity);
    }

    public TeamEntity addSlackIdentity(String teamId, String actionedByMemberId, String teamChannel) {
        // actionedByMemberId (is the now the variable name standard) = createBy = requestedById
        TeamEntity teamEntity = this.teamPersistenceHandler.addSlackIdentity(teamId,
                teamChannel);

        TeamMemberEntity teamMemberEntity = this.teamMemberPersistenceHandler
                .findByTeamMemberId(actionedByMemberId);

        this.automationHandler.teamSlackChannelCreated(teamEntity, teamMemberEntity);
        return teamEntity;
    }

    public void newDevOpsEnvironment(String teamId, String requestedById) {
        TeamEntity team = this.teamPersistenceHandler.findByTeamId(teamId);
        TeamMemberEntity requestedBy = this.teamMemberPersistenceHandler
                .findByTeamMemberId(requestedById);

        teamAssertions.assertMemberBelongsToTeam(requestedBy, team);

        this.automationHandler.devOpsEnvironmentRequested(team, requestedBy);
    }

    public void updateMembershipRequest(String teamId,
                                        MembershipRequestResource membershipRequest) {
        TeamMemberEntity approver = this.teamMemberPersistenceHandler
                .findByTeamMemberId(
                        membershipRequest.getApprovedBy().getMemberId());
        TeamEntity team = this.teamPersistenceHandler.findByTeamId(teamId);
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

        membershipRequestEntity = this.teamPersistenceHandler
                .closeMembershipRequest(
                        membershipRequest.getMembershipRequestId(),
                        membershipRequest.getRequestStatus(),
                        membershipRequest.getApprovedBy().getMemberId());

        if (membershipRequestEntity
                .getRequestStatus() == MembershipRequestStatus.APPROVED) {
            TeamMemberEntity newMemberEntity = teamMemberPersistenceHandler
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
        TeamEntity teamEntity = this.teamPersistenceHandler.findByTeamId(teamId);
        TeamMemberEntity requestedBy = this.teamMemberPersistenceHandler
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

        MembershipRequestEntity membershipRequest = this.teamPersistenceHandler
                .createMembershipRequest(teamId, requestByMemberId);

        this.automationHandler.membershipRequestCreated(teamEntity,
                membershipRequest);
    }

    public void updateTeamOpenShiftCloud(String teamId, String openShiftCloud,
                                         String requestedByMemberId) {
        TeamEntity teamEntity = this.teamPersistenceHandler.findByTeamId(teamId);
        TeamMemberEntity requestedByMemberEntity = this.teamMemberPersistenceHandler
                .findByTeamMemberId(requestedByMemberId);
        this.teamAssertions.assertMemberIsOwnerOfTeam(requestedByMemberEntity,
                teamEntity);

        String previousCloud = teamEntity.getOpenShiftCloud();

        this.teamPersistenceHandler.updateOpenShiftCloud(teamEntity,
                openShiftCloud);
        this.automationHandler.teamOpenShiftCloudUpdated(teamEntity,
                requestedByMemberEntity, previousCloud);
    }

    @Transactional(readOnly = true)
    public Set<TeamEntity> findTeamsAssociatedToProject(String projectId) {
        ProjectEntity projectEntity = projectPersistenceHandler
                .findByProjectId(projectId);
        Set<TeamEntity> associatedTeams = new HashSet<>();
        associatedTeams.add(projectEntity.getOwningTeam());
        associatedTeams.addAll(projectEntity.getTeams());
        return associatedTeams;
    }

    public TeamPersistenceHandler getTeamPersistenceHandler() {
        return teamPersistenceHandler;
    }
}
