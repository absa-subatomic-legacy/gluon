package za.co.absa.subatomic.application.team;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.adapter.team.rest.MembershipRequestResource;
import za.co.absa.subatomic.application.member.TeamMemberService;
import za.co.absa.subatomic.domain.team.*;
import za.co.absa.subatomic.infrastructure.team.view.jpa.MembershipRequestEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.MembershipRequestRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamRepository;

@Service
@Slf4j
public class TeamService {

    private CommandGateway commandGateway;

    private TeamRepository teamRepository;

    private TeamMemberService teamMemberService;

    private MembershipRequestRepository membershipRequestRepository;

    public TeamService(CommandGateway commandGateway,
            TeamRepository teamRepository,
            TeamMemberService teamMemberService,
            MembershipRequestRepository membershipRequestRepository) {
        this.commandGateway = commandGateway;
        this.teamRepository = teamRepository;
        this.teamMemberService = teamMemberService;
        this.membershipRequestRepository = membershipRequestRepository;
    }

    public String newTeam(String name, String description, String createdBy) {

        // TODO where does the check go for duplicate Teams?

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

    public String addTeamMembers(String teamId, List<String> teamOwnerIds,
            List<String> teamMemberIds) {
        return commandGateway.sendAndWait(new AddTeamMembers(
                teamId,
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

    public String newDevOpsEnvironment(String teamId, String requestedBy) {
        return commandGateway.sendAndWait(
                new NewDevOpsEnvironment(teamId, new TeamMemberId(requestedBy)),
                1,
                TimeUnit.SECONDS);
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

}
