package za.co.absa.subatomic.infrastructure.team.view.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {

    TeamEntity findByTeamId(String teamId);

    TeamEntity findByName(String name);

    List<TeamEntity> findBySlackDetailsTeamChannel(String teamChannel);

    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    List<TeamEntity> findByMembers_SlackDetailsScreenName(String screenName);

    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    List<TeamEntity> findByOwners_SlackDetailsScreenName(String screenName);

    void deleteByTeamId(String teamId);
}
