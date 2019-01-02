package za.co.absa.subatomic.infrastructure.project.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DevDeploymentEnvironmentRepository
        extends JpaRepository<DevDeploymentEnvironmentEntity, Long> {

}
