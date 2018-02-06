package za.co.absa.subatomic.infrastructure.team.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {

    TeamEntity findByTeamId(String teamId);

    TeamEntity findByName(String name);
}
