package za.co.absa.subatomic.infrastructure.project.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    ProjectEntity findByProjectId(String projectId);

    ProjectEntity findByName(String name);
}
