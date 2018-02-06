package za.co.absa.subatomic.infrastructure.project.view.jpa;

import java.util.Collections;

import org.axonframework.eventhandling.EventHandler;
import za.co.absa.subatomic.domain.project.BitbucketProject;
import za.co.absa.subatomic.domain.project.BitbucketProjectAdded;
import za.co.absa.subatomic.domain.project.BitbucketProjectRequested;
import za.co.absa.subatomic.domain.project.ProjectCreated;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProjectHandler {

    private ProjectRepository projectRepository;

    private BitbucketProjectRepository bitbucketProjectRepository;

    private TeamRepository teamRepository;

    private TeamMemberRepository teamMemberRepository;

    public ProjectHandler(ProjectRepository projectRepository,
            BitbucketProjectRepository bitbucketProjectRepository,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository) {
        this.projectRepository = projectRepository;
        this.bitbucketProjectRepository = bitbucketProjectRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @EventHandler
    @Transactional
    void on(ProjectCreated event) {
        TeamMemberEntity createdBy = teamMemberRepository
                .findByMemberId(event.getCreatedBy().getTeamMemberId());

        ProjectEntity projectEntity = ProjectEntity.builder()
                .projectId(event.getProjectId())
                .name(event.getName())
                .description(event.getDescription())
                .createdBy(createdBy)
                .teams(Collections.singleton(teamRepository
                        .findByTeamId(event.getTeam().getTeamId())))
                .build();

        projectRepository.save(projectEntity);
    }

    @EventHandler
    @Transactional
    void on(BitbucketProjectRequested event) {
        TeamMemberEntity createdBy = teamMemberRepository
                .findByMemberId(event.getRequestedBy().getTeamMemberId());

        BitbucketProject bitbucketProject = event.getBitbucketProject();
        BitbucketProjectEntity bitbucketProjectEntity = BitbucketProjectEntity
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
}
