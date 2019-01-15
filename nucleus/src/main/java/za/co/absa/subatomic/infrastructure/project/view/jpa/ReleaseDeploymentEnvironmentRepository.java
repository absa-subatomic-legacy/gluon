package za.co.absa.subatomic.infrastructure.project.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseDeploymentEnvironmentRepository
        extends JpaRepository<ReleaseDeploymentEnvironmentEntity, Long> {

}
