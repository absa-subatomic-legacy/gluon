package za.co.absa.subatomic.infrastructure.member.view.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository
        extends JpaRepository<TeamMemberEntity, Long> {

    TeamMemberEntity findByMemberId(String teamMemberId);

    TeamMemberEntity findByEmail(String email);

    TeamMemberEntity findBySlackDetailsScreenName(String slackScreenName);

    TeamMemberEntity findBySlackDetailsUserId(String slackUserId);

    TeamMemberEntity findByDomainUsername(String domainUsername);

    TeamMemberEntity findByDomainUsernameEndingWith(String domainUsername);

    List<TeamMemberEntity> findByTeams_TeamId(String teamId);

    List<TeamMemberEntity> findByMemberIdIn(List<String> memberIds);
}
