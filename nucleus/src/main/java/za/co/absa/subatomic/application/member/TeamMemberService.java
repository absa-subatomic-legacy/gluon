package za.co.absa.subatomic.application.member;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.exception.DuplicateRequestException;
import za.co.absa.subatomic.domain.member.TeamMember;
import za.co.absa.subatomic.infrastructure.member.TeamMemberAutomationHandler;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberPersistenceHandler;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;

@Service
public class TeamMemberService {

    private TeamMemberRepository teamMemberRepository;

    private TeamMemberPersistenceHandler teamMemberPersistenceHandler;

    private TeamMemberAutomationHandler teamMemberAutomationHandler;

    public TeamMemberService(
            TeamMemberRepository teamMemberRepository,
            TeamMemberPersistenceHandler teamMemberPersistenceHandler,
            TeamMemberAutomationHandler teamMemberAutomationHandler) {
        this.teamMemberRepository = teamMemberRepository;
        this.teamMemberPersistenceHandler = teamMemberPersistenceHandler;
        this.teamMemberAutomationHandler = teamMemberAutomationHandler;
    }

    public TeamMemberEntity newTeamMember(TeamMember teamMember) {
        TeamMemberEntity existingMember = this
                .findByEmail(teamMember.getEmail());
        if (existingMember != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested email address {0} is already in use.",
                    teamMember.getEmail()));
        }

        existingMember = this
                .findByDomainUsername(teamMember.getDomainUsername());
        if (existingMember != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested domain username {0} is already in use.",
                    teamMember.getDomainUsername()));
        }

        if (teamMember.getSlack() != null) {
            existingMember = this.findBySlackScreenName(
                    teamMember.getSlack().getScreenName());
            if (existingMember != null) {
                throw new DuplicateRequestException(MessageFormat.format(
                        "Requested slack username {0} is already in use.",
                        teamMember.getSlack().getScreenName()));
            }
        }

        TeamMemberEntity newTeamMember = this.teamMemberPersistenceHandler
                .createTeamMember(teamMember);

        if (newTeamMember != null) {
            this.teamMemberAutomationHandler.teamMemberCreated(newTeamMember);
        }

        return newTeamMember;
    }

    public TeamMemberEntity addSlackDetails(String memberId, String screenName,
            String userId) {
        TeamMemberEntity existingMember = this
                .findBySlackScreenName(screenName);
        if (existingMember != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested slack username {0} is already in use.",
                    screenName));
        }
        return this.teamMemberPersistenceHandler.addSlackDetails(memberId,
                screenName, userId);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberEntity> findAllTeamMembersById(
            List<String> teamMemberIdList) {
        return this.teamMemberRepository.findByMemberIdIn(teamMemberIdList);
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
