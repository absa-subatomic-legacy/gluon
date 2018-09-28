package za.co.absa.subatomic.infrastructure.member.view.jpa;

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

}
