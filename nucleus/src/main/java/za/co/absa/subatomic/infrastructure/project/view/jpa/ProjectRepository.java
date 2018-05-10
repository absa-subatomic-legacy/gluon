package za.co.absa.subatomic.infrastructure.project.view.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    ProjectEntity findByProjectId(String projectId);

    ProjectEntity findByName(String name);

    ProjectEntity findByOwningTenant_Id(String tenantId);

    List<ProjectEntity> findByTeams_Name(String teamName);
}
