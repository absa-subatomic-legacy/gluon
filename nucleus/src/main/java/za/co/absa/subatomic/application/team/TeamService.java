package za.co.absa.subatomic.application.team;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.adapter.team.rest.MembershipRequestResource;
import za.co.absa.subatomic.application.member.TeamMemberService;
import za.co.absa.subatomic.domain.exception.ApplicationAuthorisationException;
import za.co.absa.subatomic.domain.exception.DuplicateRequestException;
import za.co.absa.subatomic.domain.team.AddSlackIdentity;
import za.co.absa.subatomic.domain.team.AddTeamMembers;
import za.co.absa.subatomic.domain.team.DeleteTeam;
import za.co.absa.subatomic.domain.team.MembershipRequest;
import za.co.absa.subatomic.domain.team.MembershipRequestStatus;
import za.co.absa.subatomic.domain.team.NewMembershipRequest;
import za.co.absa.subatomic.domain.team.NewTeam;
import za.co.absa.subatomic.domain.team.NewTeamFromSlack;
import za.co.absa.subatomic.domain.team.SlackIdentity;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.domain.team.UpdateMembershipRequest;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;
import za.co.absa.subatomic.infrastructure.team.TeamAutomationHandler;
import za.co.absa.subatomic.infrastructure.team.view.jpa.MembershipRequestEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.MembershipRequestRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamRepository;

@Service
@Slf4j
public class TeamService {

    private CommandGateway commandGateway;

    private TeamRepository teamRepository;

    private ProjectRepository projectRepository;

    private TeamMemberService teamMemberService;

    private TeamAutomationHandler automationHandler;

    private MembershipRequestRepository membershipRequestRepository;

    public TeamService(CommandGateway commandGateway,
            TeamRepository teamRepository,
            MembershipRequestRepository membershipRequestRepository,
            ProjectRepository projectRepository,
            TeamMemberService teamMemberService,
            TeamAutomationHandler automationHandler) {
        this.commandGateway = commandGateway;
        this.teamRepository = teamRepository;
        this.membershipRequestRepository = membershipRequestRepository;
        this.projectRepository = projectRepository;
        this.teamMemberService = teamMemberService;
        this.automationHandler = automationHandler;
    }

    public String newTeam(String name, String description, String createdBy) {
        TeamEntity existingTeam = this.findByName(name);
        if (existingTeam != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested team name {0} is not available.",
                    name));
        }

        return commandGateway.sendAndWait(
                new NewTeam(
                        UUID.randomUUID().toString(),
                        name,
                        description,
                        new TeamMemberId(createdBy)),
                1,
                TimeUnit.SECONDS);
    }

    public String newTeamFromSlack(String name, String description,
            String createdBy,
            String teamChannel) {
        TeamEntity existingTeam = this.findByName(name);
        if (existingTeam != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested team name {0} is not available.",
                    name));
        }
        // TODO use the TeamMemberService to add a member for the owner

        return commandGateway.sendAndWait(
                new NewTeamFromSlack(new NewTeam(
                        UUID.randomUUID().toString(),
                        name,
                        description,
                        new TeamMemberId(createdBy)),
                        new SlackIdentity(teamChannel)),
                1,
                TimeUnit.SECONDS);

    }

    public String addTeamMembers(String teamId, String actionedBy,
            List<String> teamOwnerIds,
            List<String> teamMemberIds) {
        return commandGateway.sendAndWait(new AddTeamMembers(
                teamId,
                new TeamMemberId(actionedBy),
                teamOwnerIds,
                teamMemberIds),
                1,
                TimeUnit.SECONDS);
    }

    public String removeTeamMembers(String teamId, String actionedBy,
            List<String> teamOwnerIds,
            List<String> teamMemberIds) {
        return commandGateway.sendAndWait(new AddTeamMembers(
                teamId,
                new TeamMemberId(actionedBy),
                teamOwnerIds,
                teamMemberIds),
                1,
                TimeUnit.SECONDS);
    }

    public String addSlackIdentity(String teamId, String teamChannel) {
        return commandGateway.sendAndWait(new AddSlackIdentity(
                teamId,
                teamChannel),
                1,
                TimeUnit.SECONDS);
    }

    public void newDevOpsEnvironment(String teamId, String requestedById) {
        TeamEntity team = this.teamRepository.findByTeamId(teamId);
        TeamMemberEntity requestedBy = this.teamMemberService
                .findByTeamMemberId(requestedById);

        assertMemberBelongsToTeam(requestedBy, team);

        this.automationHandler.devOpsEnvironmentRequested(team, requestedBy);
    }

    public String updateMembershipRequest(String teamId,
            MembershipRequestResource membershipRequest) {
        return commandGateway.sendAndWait(
                new UpdateMembershipRequest(
                        teamId,
                        new MembershipRequest(
                                membershipRequest.getMembershipRequestId(),
                                null, // requested by is not required
                                new TeamMemberId(membershipRequest
                                        .getApprovedBy().getMemberId()),
                                membershipRequest.getRequestStatus())),
                1,
                TimeUnit.SECONDS);
    }

    public String newMembershipRequest(String teamId,
            String requestByMemberId) {

        return commandGateway.sendAndWait(
                new NewMembershipRequest(
                        teamId,
                        new MembershipRequest(UUID.randomUUID().toString(),
                                new TeamMemberId(requestByMemberId),
                                null,
                                MembershipRequestStatus.OPEN)),
                1,
                TimeUnit.SECONDS);
    }

    public String deleteTeam(String teamId) {
        return commandGateway.sendAndWait(
                new DeleteTeam(teamId),
                1,
                TimeUnit.SECONDS);
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

    @Transactional(readOnly = true)
    public TeamEntity findByTeamId(String teamId) {
        return teamRepository.findByTeamId(teamId);
    }

    @Transactional(readOnly = true)
    public List<TeamEntity> findAll() {
        return teamRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TeamEntity findByName(String name) {
        return teamRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public MembershipRequestEntity findMembershipRequestById(String id) {
        return membershipRequestRepository.findByMembershipRequestId(id);
    }

    @Transactional(readOnly = true)
    public Set<TeamEntity> findByMemberOrOwnerMemberId(String teamMemberId) {
        Set<TeamEntity> teamsWithMemberOrOwner = new HashSet<>();
        teamsWithMemberOrOwner.addAll(teamRepository
                .findByMembers_MemberId(teamMemberId));
        teamsWithMemberOrOwner.addAll(teamRepository
                .findByOwners_MemberId(teamMemberId));
        return teamsWithMemberOrOwner;
    }

    @Transactional(readOnly = true)
    public Set<TeamEntity> findByMemberOrOwnerSlackScreenName(
            String slackScreenName) {
        Set<TeamEntity> teamsWithMemberOrOwner = new HashSet<>();
        teamsWithMemberOrOwner.addAll(teamRepository
                .findByMembers_SlackDetailsScreenName(slackScreenName));
        teamsWithMemberOrOwner.addAll(teamRepository
                .findByOwners_SlackDetailsScreenName(slackScreenName));
        return teamsWithMemberOrOwner;
    }

    @Transactional(readOnly = true)
    public List<TeamEntity> findBySlackTeamChannel(String slackTeamChannel) {
        return teamRepository.findBySlackDetailsTeamChannel(slackTeamChannel);
    }

    private void assertMemberBelongsToTeam(TeamMemberEntity memberEntity,
            TeamEntity teamEntity) {
        Set<TeamMemberEntity> allMembers = new HashSet<>();
        allMembers.addAll(teamEntity.getMembers());
        allMembers.addAll(teamEntity.getOwners());

        if (allMembers.stream().map(TeamMemberEntity::getMemberId)
                .noneMatch(memberEntity.getMemberId()::equals)) {
            throw new ApplicationAuthorisationException(MessageFormat.format(
                    "TeamMember with id {0} is not a member of the team with id {1}.",
                    memberEntity.getMemberId(),
                    teamEntity.getTeamId()));
        }
    }
}
