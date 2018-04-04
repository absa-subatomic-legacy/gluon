package za.co.absa.subatomic.infrastructure.pkg.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageRepository
        extends JpaRepository<PackageEntity, Long> {

    PackageEntity findByApplicationId(String applicationId);

    List<PackageEntity> findByName(String name);

    PackageEntity findByNameAndProjectName(String name, String projectName);

    PackageEntity findByNameAndProjectProjectId(String name, String projectId);

    List<PackageEntity> findByProjectId(Long projectId);
}
