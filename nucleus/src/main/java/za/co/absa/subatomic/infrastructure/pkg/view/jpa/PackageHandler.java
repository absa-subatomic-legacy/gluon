package za.co.absa.subatomic.infrastructure.pkg.view.jpa;

import org.axonframework.eventhandling.EventHandler;
import za.co.absa.subatomic.domain.pkg.PackageCreated;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PackageHandler {

    private ProjectRepository projectRepository;

    private TeamMemberRepository teamMemberRepository;

    private PackageRepository packageRepository;

    public PackageHandler(ProjectRepository projectRepository,
            TeamMemberRepository teamMemberRepository,
            PackageRepository packageRepository) {
        this.projectRepository = projectRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.packageRepository = packageRepository;
    }

    @EventHandler
    @Transactional
    void on(PackageCreated event) {
        TeamMemberEntity createdBy = teamMemberRepository
                .findByMemberId(event.getCreatedBy().getTeamMemberId());

        PackageEntity packageEntity = PackageEntity.builder()
                .applicationId(event.getPackageId())
                .packageType(event.getPackageType())
                .name(event.getName())
                .description(event.getDescription())
                .createdBy(createdBy)
                .project(projectRepository
                        .findByProjectId(event.getProject().getProjectId()))
                .build();

        packageRepository.save(packageEntity);
    }
}
