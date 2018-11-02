package za.co.absa.subatomic.infrastructure.prod.project.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.absa.subatomic.domain.prod.project.ProjectProductionRequestStatus;

import java.util.List;

public interface ProjectProdRequestRepository
        extends JpaRepository<ProjectProdRequestEntity, Long> {

    ProjectProdRequestEntity findByProjectProdRequestId(String id);

    List<ProjectProdRequestEntity> findByApprovalStatus(ProjectProductionRequestStatus status);

    List<ProjectProdRequestEntity> findByProjectProjectId(String projectId);
}
