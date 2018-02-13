package za.co.absa.subatomic.application.member;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import za.co.absa.subatomic.domain.member.AddSlackDetails;
import za.co.absa.subatomic.domain.member.NewTeamMember;
import za.co.absa.subatomic.domain.member.NewTeamMemberFromSlack;
import za.co.absa.subatomic.domain.member.SlackIdentity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamMemberService {

    private CommandGateway commandGateway;

    private TeamMemberRepository teamMemberRepository;

    public TeamMemberService(CommandGateway commandGateway,
            TeamMemberRepository teamMemberRepository) {
        this.commandGateway = commandGateway;
        this.teamMemberRepository = teamMemberRepository;
    }

    public String newTeamMember(String firstName, String lastName,
            String email, String domainUsername) {
        return commandGateway.sendAndWait(
                new NewTeamMember(
                        UUID.randomUUID().toString(),
                        firstName,
                        lastName,
                        email,
                        domainUsername),
                1000,
                TimeUnit.SECONDS);
    }

    public String newTeamMemberFromSlack(String firstName, String lastName,
            String email, String domainUsername, String screeName,
            String userId) {

        return commandGateway.sendAndWait(
                new NewTeamMemberFromSlack(
                        new NewTeamMember(
                                UUID.randomUUID().toString(),
                                firstName,
                                lastName,
                                email,
                                domainUsername),
                        new SlackIdentity(screeName, userId)),
                1,
                TimeUnit.SECONDS);
    }

    public String addSlackDetails(String memberId, String screenName,
            String userId) {
        return commandGateway.sendAndWait(new AddSlackDetails(
                memberId,
                screenName,
                userId),
                1,
                TimeUnit.SECONDS);
    }

    @Transactional(readOnly = true)
    public TeamMemberEntity findByTeamId(String teamMemberId) {
        return teamMemberRepository.findByMemberId(teamMemberId);
    }

    @Transactional(readOnly = true)
    public TeamMemberEntity findByEmail(String email) {
        return teamMemberRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberEntity> findAll() {
        return teamMemberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TeamMemberEntity findBySlackScreenName(String slackScreenName) {
        return teamMemberRepository
                .findBySlackDetailsScreenName(slackScreenName);
    }
}