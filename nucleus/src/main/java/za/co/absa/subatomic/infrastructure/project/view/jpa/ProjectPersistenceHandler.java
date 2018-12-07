package za.co.absa.subatomic.infrastructure.project.view.jpa;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.project.BitbucketProject;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;

@Component
public class ProjectPersistenceHandler {

    private ProjectRepository projectRepository;

    private BitbucketProjectRepository bitbucketProjectRepository;

    public ProjectPersistenceHandler(ProjectRepository projectRepository,
            BitbucketProjectRepository bitbucketProjectRepository) {
        this.projectRepository = projectRepository;
        this.bitbucketProjectRepository = bitbucketProjectRepository;
    }

    @Transactional
    public ProjectEntity createProject(String name, String description,
            TeamMemberEntity createdBy, TeamEntity owningTeam,
            TenantEntity owningTenant) {
        ProjectEntity project = ProjectEntity
                .builder()
                .projectId(UUID.randomUUID().toString())
                .name(name)
                .description(description)
                .createdBy(createdBy)
                .owningTeam(owningTeam)
                .owningTenant(owningTenant)
                .build();

        return this.projectRepository.save(project);
    }

    @Transactional
    public ProjectEntity linkBitbucketProject(String projectId,
            BitbucketProject bitbucketProject, TeamMemberEntity createdBy) {

        BitbucketProjectEntity bitbucketProjectEntity = BitbucketProjectEntity
                .builder()
                .bitbucketProjectId(bitbucketProject.getId())
                .url(bitbucketProject.getUrl())
                .key(bitbucketProject.getKey())
                .name(bitbucketProject.getName())
                .description(bitbucketProject.getDescription())
                .createdBy(createdBy)
                .build();

        ProjectEntity projectEntity = projectRepository
                .findByProjectId(projectId);
        projectEntity.setBitbucketProject(bitbucketProjectEntity);

        bitbucketProjectRepository.save(bitbucketProjectEntity);

        return projectRepository.save(projectEntity);
    }

    @Transactional
    public ProjectEntity linkTeamsToProject(String projectId,
            List<TeamEntity> teamEntities) {

        ProjectEntity project = projectRepository
                .findByProjectId(projectId);

        project.getTeams().addAll(teamEntities);

        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(String projectId) {
        projectRepository
                .deleteByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public ProjectEntity findByProjectId(String projectId) {
        return projectRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<ProjectEntity> findAllProjects() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ProjectEntity findByName(String name) {
        return projectRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<ProjectEntity> findByTeamName(String teamName) {
        return projectRepository.findByTeams_Name(teamName);
    }

    @Transactional(readOnly = true)
    public Collection<TeamEntity> findTeamsAssociatedToProject(
            String projectId) {
        return projectRepository.findByProjectId(projectId).getTeams();
    }
}
