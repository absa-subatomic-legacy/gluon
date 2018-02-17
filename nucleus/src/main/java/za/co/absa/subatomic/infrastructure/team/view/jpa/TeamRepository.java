package za.co.absa.subatomic.infrastructure.team.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {

    TeamEntity findByTeamId(String teamId);

    TeamEntity findByName(String name);

    List<TeamEntity> findByMembers_SlackDetailsScreenName(String screenName);

    List<TeamEntity> findByOwners_SlackDetailsScreenName(String screenName);
}
