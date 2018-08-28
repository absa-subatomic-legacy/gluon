package za.co.absa.subatomic.application.openshift;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.adapter.openshift.rest.OpenShiftResource;
import za.co.absa.subatomic.infrastructure.openshift.view.jpa.OpenShiftResourceEntity;
import za.co.absa.subatomic.infrastructure.openshift.view.jpa.OpenShiftResourceRepository;

@Service
public class OpenShiftResourceService {

    private OpenShiftResourceRepository openShiftResourceRepository;

    public OpenShiftResourceService(
            OpenShiftResourceRepository openShiftResourceRepository) {

        this.openShiftResourceRepository = openShiftResourceRepository;
    }

    public List<OpenShiftResourceEntity> createAllOpenShiftEntities(
            Collection<OpenShiftResource> openShiftResources) {
        List<OpenShiftResourceEntity> openShiftResourceEntities = new ArrayList<>();

        for (OpenShiftResource resource : openShiftResources) {
            openShiftResourceEntities.add(
                    OpenShiftResourceEntity.builder()
                            .openshiftResourceId(UUID.randomUUID().toString())
                            .kind(resource.getKind())
                            .name(resource.getName())
                            .resourceDetails(resource.getResourceDetails())
                            .build());
        }

        return this.openShiftResourceRepository.save(openShiftResourceEntities);
    }

    @Transactional(readOnly = true)
    public OpenShiftResourceEntity findByOpenshiftResourceId(String id) {
        return this.openShiftResourceRepository.findByOpenshiftResourceId(id);
    }
}
