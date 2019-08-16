package za.co.absa.subatomic.infrastructure.project.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdditionalEnvironmentRepository
        extends JpaRepository<DevDeploymentEnvironmentEntity, Long> {

}
