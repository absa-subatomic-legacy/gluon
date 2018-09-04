package za.co.absa.subatomic.infrastructure.member.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository
        extends JpaRepository<TeamMemberEntity, Long> {

    TeamMemberEntity findByMemberId(String teamMemberId);

    TeamMemberEntity findByEmail(String email);

    TeamMemberEntity findBySlackDetailsScreenName(String slackScreenName);

    TeamMemberEntity findByDomainUsername(String domainUsername);

    List<TeamMemberEntity> findByTeams_TeamId(String teamId);
}
