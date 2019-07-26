package za.co.absa.subatomic.infrastructure.team.view.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {

    TeamEntity findByTeamId(String teamId);

    TeamEntity findByName(String name);

    List<TeamEntity> findBySlackDetailsTeamChannel(String teamChannel);

    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    List<TeamEntity> findByMembers_SlackDetailsUserId(String slackUserId);

    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    List<TeamEntity> findByOwners_SlackDetailsUserId(String slackUserId);

    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    List<TeamEntity> findByMembers_MemberId(String teamMemberId);

    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    List<TeamEntity> findByOwners_MemberId (String teamMemberId);

    void deleteByTeamId(String teamId);
}
