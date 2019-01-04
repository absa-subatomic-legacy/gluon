package za.co.absa.subatomic.application.member;

import java.text.MessageFormat;

import org.springframework.stereotype.Service;

import za.co.absa.subatomic.domain.exception.DuplicateRequestException;
import za.co.absa.subatomic.domain.member.TeamMember;
import za.co.absa.subatomic.infrastructure.member.TeamMemberAutomationHandler;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberPersistenceHandler;

@Service
public class TeamMemberService {

    private TeamMemberPersistenceHandler teamMemberPersistenceHandler;

    private TeamMemberAutomationHandler teamMemberAutomationHandler;

    public TeamMemberService(
            TeamMemberPersistenceHandler teamMemberPersistenceHandler,
            TeamMemberAutomationHandler teamMemberAutomationHandler) {
        this.teamMemberPersistenceHandler = teamMemberPersistenceHandler;
        this.teamMemberAutomationHandler = teamMemberAutomationHandler;
    }

    public TeamMemberEntity newTeamMember(TeamMember teamMember) {
        TeamMemberEntity existingMember = this.teamMemberPersistenceHandler
                .findByEmail(teamMember.getEmail());
        if (existingMember != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested email address {0} is already in use.",
                    teamMember.getEmail()));
        }

        existingMember = this.teamMemberPersistenceHandler
                .findByDomainUsername(teamMember.getDomainUsername());
        if (existingMember != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested domain username {0} is already in use.",
                    teamMember.getDomainUsername()));
        }

        if (teamMember.getSlack() != null) {
            existingMember = this.teamMemberPersistenceHandler
                    .findBySlackScreenName(
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
        TeamMemberEntity existingMember = this.teamMemberPersistenceHandler
                .findBySlackScreenName(screenName);
        if (existingMember != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Requested slack username {0} is already in use.",
                    screenName));
        }
        return this.teamMemberPersistenceHandler.addSlackDetails(memberId,
                screenName, userId);
    }

    public TeamMemberPersistenceHandler getTeamMemberPersistenceHandler() {
        return teamMemberPersistenceHandler;
    }
}
