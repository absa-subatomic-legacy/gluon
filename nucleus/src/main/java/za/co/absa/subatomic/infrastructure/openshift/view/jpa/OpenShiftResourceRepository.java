package za.co.absa.subatomic.infrastructure.openshift.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OpenShiftResourceRepository
        extends JpaRepository<OpenShiftResourceEntity, Long> {
    OpenShiftResourceEntity findByOpenShiftResourceId(String openShiftResourceId);
}
