package za.co.absa.subatomic.application.pkg;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import za.co.absa.subatomic.domain.pkg.NewPackage;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.pkg.view.jpa.PackageEntity;
import za.co.absa.subatomic.infrastructure.pkg.view.jpa.PackageRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@Service
public class PackageService {

    private CommandGateway commandGateway;

    private PackageRepository packageRepository;

    private ProjectRepository projectRepository;

    public PackageService(CommandGateway commandGateway,
            PackageRepository packageRepository,
            ProjectRepository projectRepository) {
        this.commandGateway = commandGateway;
        this.packageRepository = packageRepository;
        this.projectRepository = projectRepository;
    }

    public String newApplication(String packageType, String name,
            String description, String createdBy, String projectId) {

        Set<TeamEntity> projectAssociatedTeams = findTeamsByProjectId(
                projectId);

        Set<String> allMemberAndOwnerIds = getAllMemberAndOwnerIds(projectAssociatedTeams);

        return commandGateway.sendAndWait(
                new NewPackage(
                        UUID.randomUUID().toString(),
                        packageType,
                        name,
                        description,
                        new TeamMemberId(createdBy),
                        new ProjectId(projectId),
                        allMemberAndOwnerIds),
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

    @Transactional(readOnly = true)
    public Set<TeamEntity> findTeamsByProjectId(String projectId) {
        return projectRepository.findByProjectId(projectId).getTeams();
    }

    private Set<String> getAllMemberAndOwnerIds(Collection<TeamEntity> teams){
        Set<String> teamMemberIds = new HashSet<>();
        for (TeamEntity team: teams){
            for (TeamMemberEntity member: team.getMembers()){
                teamMemberIds.add(member.getMemberId());
            }
            for (TeamMemberEntity owner: team.getOwners()){
                teamMemberIds.add(owner.getMemberId());
            }
        }
        return teamMemberIds;
    }
}
