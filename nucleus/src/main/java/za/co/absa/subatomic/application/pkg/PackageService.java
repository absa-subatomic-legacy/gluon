package za.co.absa.subatomic.application.pkg;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.exception.DuplicateRequestException;
import za.co.absa.subatomic.domain.pkg.NewPackage;
import za.co.absa.subatomic.domain.pkg.ProjectId;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.pkg.view.jpa.PackageEntity;
import za.co.absa.subatomic.infrastructure.pkg.view.jpa.PackageRepository;
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
        PackageEntity existingPackage = this.findByNameAndProjectName(name,
                projectId);
        if (existingPackage != null) {
            throw new DuplicateRequestException(MessageFormat.format(
                    "Package with name {0} already exists in project with id {1}.",
                    name, projectId));
        }

        Set<TeamEntity> projectAssociatedTeams = findTeamsByProjectId(
                projectId);

        Set<String> allMemberAndOwnerIds = getAllMemberAndOwnerIds(
                projectAssociatedTeams);

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
    public PackageEntity findByApplicationId(String applicationId) {
        return packageRepository.findByApplicationId(applicationId);
    }

    @Transactional(readOnly = true)
    public List<PackageEntity> findAll() {
        return packageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PackageEntity> findByName(String name) {
        return packageRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public PackageEntity findByNameAndProjectName(String name,
            String projectName) {
        return packageRepository.findByNameAndProjectName(name, projectName);
    }

    @Transactional(readOnly = true)
    public PackageEntity findByNameAndProjectId(String name, String projectId) {
        return packageRepository.findByNameAndProjectId(name,
                Long.valueOf(projectId));
    }

    @Transactional(readOnly = true)
    public Set<TeamEntity> findTeamsByProjectId(String projectId) {
        return projectRepository.findByProjectId(projectId).getTeams();
    }

    private Set<String> getAllMemberAndOwnerIds(Collection<TeamEntity> teams) {
        Set<String> teamMemberIds = new HashSet<>();
        for (TeamEntity team : teams) {
            for (TeamMemberEntity member : team.getMembers()) {
                teamMemberIds.add(member.getMemberId());
            }
            for (TeamMemberEntity owner : team.getOwners()) {
                teamMemberIds.add(owner.getMemberId());
            }
        }
        return teamMemberIds;
    }
}
