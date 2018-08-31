package za.co.absa.subatomic.infrastructure.prod.project.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectProdRequestRepository
        extends JpaRepository<ProjectProdRequestEntity, Long> {

    ProjectProdRequestEntity findByProjectProdRequestId(String id);
}
