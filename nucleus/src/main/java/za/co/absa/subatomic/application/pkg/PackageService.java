package za.co.absa.subatomic.application.pkg;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import za.co.absa.subatomic.domain.pkg.NewPackage;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.pkg.view.jpa.PackageEntity;
import za.co.absa.subatomic.infrastructure.pkg.view.jpa.PackageRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PackageService {

    private CommandGateway commandGateway;

    private PackageRepository packageRepository;

    public PackageService(CommandGateway commandGateway,
            PackageRepository packageRepository) {
        this.commandGateway = commandGateway;
        this.packageRepository = packageRepository;
    }

    public String newApplication(String packageType, String name,
            String description, String createdBy, String project) {
        return commandGateway.sendAndWait(
                new NewPackage(
                        UUID.randomUUID().toString(),
                        packageType,
                        name,
                        description,
                        new TeamMemberId(createdBy),
                        new ProjectId(project)),
                1000,
                TimeUnit.SECONDS);
    }

    @Transactional(readOnly = true)
    public PackageEntity findByProjectId(String packageId) {
        return packageRepository.findByApplicationId(packageId);
    }

    @Transactional(readOnly = true)
    public List<PackageEntity> findAll() {
        return packageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PackageEntity findByName(String name) {
        return packageRepository.findByName(name);
    }
}
