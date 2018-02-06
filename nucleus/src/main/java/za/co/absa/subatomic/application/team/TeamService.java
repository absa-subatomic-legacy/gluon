package za.co.absa.subatomic.application.team;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import za.co.absa.subatomic.application.member.TeamMemberService;
import za.co.absa.subatomic.domain.team.AddSlackIdentity;
import za.co.absa.subatomic.domain.team.AddTeamMembers;
import za.co.absa.subatomic.domain.team.NewDevOpsEnvironment;
import za.co.absa.subatomic.domain.team.NewTeam;
import za.co.absa.subatomic.domain.team.NewTeamFromSlack;
import za.co.absa.subatomic.domain.team.SlackIdentity;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TeamService {

    private CommandGateway commandGateway;

    private TeamRepository teamRepository;

    private TeamMemberService teamMemberService;

    public TeamService(CommandGateway commandGateway,
            TeamRepository teamRepository,
            TeamMemberService teamMemberService) {
        this.commandGateway = commandGateway;
        this.teamRepository = teamRepository;
        this.teamMemberService = teamMemberService;
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

}
