package za.co.absa.subatomic.infrastructure.project.view.jpa;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import za.co.absa.subatomic.domain.project.*;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamRepository;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantRepository;

@Component
public class ProjectHandler {

    private ProjectRepository projectRepository;

    private BitbucketProjectRepository bitbucketProjectRepository;

    private TeamRepository teamRepository;

    private TeamMemberRepository teamMemberRepository;

    private TenantRepository tenantRepository;

    public ProjectHandler(ProjectRepository projectRepository,
            BitbucketProjectRepository bitbucketProjectRepository,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            TenantRepository tenantRepository) {
        this.projectRepository = projectRepository;
        this.bitbucketProjectRepository = bitbucketProjectRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.tenantRepository = tenantRepository;
    }

    @EventHandler
    @Transactional
    void on(ProjectCreated event) {
        TeamMemberEntity createdBy = teamMemberRepository
                .findByMemberId(event.getCreatedBy().getTeamMemberId());

        TeamEntity creatingTeam = teamRepository
                .findByTeamId(event.getTeam().getTeamId());

        TenantEntity owningTenant = tenantRepository
                .findByTenantId(event.getTenant().getTenantId());

        ProjectEntity projectEntity = ProjectEntity.builder()
                .projectId(event.getProjectId())
                .name(event.getName())
                .description(event.getDescription())
                .createdBy(createdBy)
                .owningTeam(creatingTeam)
                .owningTenant(owningTenant)
                .teams(Collections.singleton(creatingTeam))
                .build();

        projectRepository.save(projectEntity);
    }

    @EventHandler
    @Transactional
    void on(BitbucketProjectRequested event) {
        BitbucketProjectEntity bitbucketProjectEntity = bitbucketProjectRepository
                .findByKey(event.getBitbucketProject().getKey());
        if (bitbucketProjectEntity == null) {
            TeamMemberEntity createdBy = teamMemberRepository
                    .findByMemberId(event.getRequestedBy().getTeamMemberId());

            BitbucketProject bitbucketProject = event.getBitbucketProject();
            bitbucketProjectEntity = BitbucketProjectEntity
                    .builder()
                    .key(bitbucketProject.getKey())
                    .name(bitbucketProject.getName())
                    .description(bitbucketProject.getDescription())
                    .createdBy(createdBy)
                    .build();

            ProjectEntity projectEntity = projectRepository
                    .findByProjectId(event.getProjectId().getProjectId());
            projectEntity.setBitbucketProject(bitbucketProjectEntity);

            bitbucketProjectRepository.save(bitbucketProjectEntity);
        }
    }

    @EventHandler
    @Transactional
    void on(BitbucketProjectAdded event) {
        BitbucketProject bitbucketProject = event.getBitbucketProject();
        BitbucketProjectEntity bitbucketProjectEntity = bitbucketProjectRepository
                .findByKey(bitbucketProject.getKey());
        bitbucketProjectEntity.setBitbucketProjectId(bitbucketProject.getId());
        bitbucketProjectEntity.setUrl(bitbucketProject.getUrl());

        bitbucketProjectRepository.save(bitbucketProjectEntity);
    }

    @EventHandler
    @Transactional
    void on(BitbucketProjectLinked event) {
        BitbucketProject bitbucketProject = event.getBitbucketProject();

        TeamMemberEntity createdBy = teamMemberRepository
                .findByMemberId(event.getRequestedBy().getTeamMemberId());

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
                .findByProjectId(event.getProjectId().getProjectId());
        projectEntity.setBitbucketProject(bitbucketProjectEntity);

        bitbucketProjectRepository.save(bitbucketProjectEntity);
    }

    @EventHandler
    @Transactional
    void on(TeamsLinkedToProject event) {

        ProjectEntity project = projectRepository.findByProjectId(event.getProjectId().getProjectId());

        Set<TeamEntity> teamEntities = new HashSet<>();

        for(TeamId team: event.getTeamsLinked()){
            teamEntities.add(teamRepository.findByTeamId(team.getTeamId()));
        }

        project.getTeams().addAll(teamEntities);

        projectRepository.save(project);
    }
}
