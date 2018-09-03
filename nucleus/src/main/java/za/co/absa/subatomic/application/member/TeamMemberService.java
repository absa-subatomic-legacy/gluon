package za.co.absa.subatomic.application.member;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import za.co.absa.subatomic.domain.exception.DuplicateRequestException;
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
        TeamMemberEntity existingMember = this.findByEmail(email);
        if (existingMember != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested email address {0} is already in use.",
                    email));
        }

        existingMember = this.findByDomainUsername(domainUsername);
        if (existingMember != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested domain username {0} is already in use.",
                    domainUsername));
        }
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
            String email, String domainUsername, String screenName,
            String userId) {
        TeamMemberEntity existingMember = this.findByEmail(email);
        if (existingMember != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested email address {0} is already in use.",
                    email));
        }

        existingMember = this.findByDomainUsername(domainUsername);
        if (existingMember != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested domain username {0} is already in use.",
                    domainUsername));
        }

        existingMember = this.findBySlackScreenName(screenName);
        if (existingMember != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested slack username {0} is already in use.",
                    screenName));
        }
        return commandGateway.sendAndWait(
                new NewTeamMemberFromSlack(
                        new NewTeamMember(
                                UUID.randomUUID().toString(),
                                firstName,
                                lastName,
                                email,
                                domainUsername),
                        new SlackIdentity(screenName, userId)),
                1,
                TimeUnit.SECONDS);
    }

    public String addSlackDetails(String memberId, String screenName,
            String userId) {
        TeamMemberEntity existingMember = this.findBySlackScreenName(screenName);
        if (existingMember != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested slack username {0} is already in use.",
                    screenName));
        }
        return commandGateway.sendAndWait(new AddSlackDetails(
                memberId,
                screenName,
                userId),
                1,
                TimeUnit.SECONDS);
    }

    @Transactional(readOnly = true)
    public TeamMemberEntity findByTeamMemberId(String teamMemberId) {
        return teamMemberRepository.findByMemberId(teamMemberId);
    }

    @Transactional(readOnly = true)
    public TeamMemberEntity findByEmail(String email) {
        return teamMemberRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public TeamMemberEntity findByDomainUsername(String domainUsername) {
        return teamMemberRepository.findByDomainUsername(domainUsername);
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

    @Transactional(readOnly = true)
    public List<TeamMemberEntity> findMembersAssociatedToTeam(String teamId) {
        return teamMemberRepository
                .findByTeams_TeamId(teamId);
    }
}
