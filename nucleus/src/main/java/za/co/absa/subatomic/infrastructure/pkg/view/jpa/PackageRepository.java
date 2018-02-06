package za.co.absa.subatomic.infrastructure.pkg.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageRepository
        extends JpaRepository<PackageEntity, Long> {

    PackageEntity findByApplicationId(String applicationId);

    PackageEntity findByName(String name);
}
