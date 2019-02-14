package za.co.absa.subatomic.infrastructure.member.view.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.member.TeamMember;

@Component
public class TeamMemberPersistenceHandler {

    private TeamMemberRepository teamMemberRepository;

    public TeamMemberPersistenceHandler(
            TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    @Transactional
    public TeamMemberEntity createTeamMember(TeamMember newTeamMember) {
        TeamMemberEntity teamMemberEntity = TeamMemberEntity.builder()
                .memberId(UUID.randomUUID().toString())
                .firstName(newTeamMember.getFirstName())
                .lastName(newTeamMember.getLastName())
                .email(newTeamMember.getEmail())
                .domainUsername(newTeamMember.getDomainUsername())
                .build();

        if (newTeamMember.getSlack() != null) {
            teamMemberEntity.setSlackDetails(
                    new SlackDetailsEmbedded(
                            newTeamMember.getSlack().getScreenName(),
                            newTeamMember.getSlack().getUserId()));
        }

        return teamMemberRepository.save(teamMemberEntity);
    }

    @Transactional
    public TeamMemberEntity addSlackDetails(String memberId, String screenName,
            String userId) {
        TeamMemberEntity teamMember = teamMemberRepository
                .findByMemberId(memberId);
        teamMember.setSlackDetails(new SlackDetailsEmbedded(
                screenName,
                userId));
        return teamMemberRepository.save(teamMember);
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
    public TeamMemberEntity findByDomainUsernameWithOrWithoutDomain(
            String domainUsername) {
        TeamMemberEntity entity = null;
        if (!domainUsername.contains("\\")) {
            entity = teamMemberRepository
                    .findByDomainUsernameLike("\\" + domainUsername);
        }
        if (entity == null) {
            entity = teamMemberRepository.findByDomainUsername(domainUsername);
        }
        return entity;
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
