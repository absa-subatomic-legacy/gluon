package za.co.absa.subatomic.infrastructure.prod.project.view.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import za.co.absa.subatomic.domain.prod.project.ProjectProductionRequestStatus;

public interface ProjectProdRequestRepository
        extends JpaRepository<ProjectProdRequestEntity, Long> {

    ProjectProdRequestEntity findByProjectProdRequestId(String id);

    List<ProjectProdRequestEntity> findByApprovalStatus(
            ProjectProductionRequestStatus status);

    List<ProjectProdRequestEntity> findByProjectProjectId(String projectId);

    List<ProjectProdRequestEntity> findByProjectProjectIdAndDeploymentPipelinePipelineId(
            String projectId, String pipelineId);

    List<ProjectProdRequestEntity> findByProjectProjectIdAndDeploymentPipelinePipelineIdAndApprovalStatus(
            String projectId, String pipelineId,
            ProjectProductionRequestStatus status);
}
