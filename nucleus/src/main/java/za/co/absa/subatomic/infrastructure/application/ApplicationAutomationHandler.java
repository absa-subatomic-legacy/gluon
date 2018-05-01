package za.co.absa.subatomic.infrastructure.application;

import java.util.ArrayList;
import java.util.List;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.domain.application.ApplicationCreated;
import za.co.absa.subatomic.domain.application.ApplicationEnvironmentRequested;
import za.co.absa.subatomic.domain.application.BitbucketGitRepository;
import za.co.absa.subatomic.domain.member.SlackIdentity;
import za.co.absa.subatomic.domain.project.BitbucketProject;
import za.co.absa.subatomic.domain.project.ProjectCreated;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationEntity;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationRepository;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@Component
@Slf4j
public class ApplicationAutomationHandler {

    private RestTemplate restTemplate;

    private ApplicationRepository applicationRepository;

    private ProjectRepository projectRepository;

    private TeamMemberRepository teamMemberRepository;

    private AtomistConfigurationProperties atomistConfigurationProperties;

    public ApplicationAutomationHandler(RestTemplate restTemplate,
            ApplicationRepository applicationRepository,
            ProjectRepository projectRepository,
            TeamMemberRepository teamMemberRepository,
            AtomistConfigurationProperties atomistConfigurationProperties) {
        this.restTemplate = restTemplate;
        this.applicationRepository = applicationRepository;
        this.projectRepository = projectRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.atomistConfigurationProperties = atomistConfigurationProperties;
    }

    @EventHandler
    void on(ApplicationEnvironmentRequested event) {
        log.info(
                "An application environment was requested for project [{}], sending event to Atomist: {}",
                event.getProjectId().getProjectId(), event);

        ApplicationEntity applicationEntity = applicationRepository
                .findByApplicationId(
                        event.getApplicationId().getApplicationId());

        ProjectEntity projectEntity = projectRepository
                .findByProjectId(event.getProjectId().getProjectId());

        TeamMemberEntity teamMemberEntity = teamMemberRepository
                .findByMemberId(event.getRequestedBy().getTeamMemberId());

        SlackIdentity slackIdentity = null;
        if (teamMemberEntity.getSlackDetails() != null) {
            slackIdentity = new SlackIdentity(
                    teamMemberEntity.getSlackDetails()
                            .getScreenName(),
                    teamMemberEntity.getSlackDetails()
                            .getUserId());
        }

        List<Team> teamList = new ArrayList<>();
        for (TeamEntity teamEntity : projectEntity.getTeams()) {
            za.co.absa.subatomic.domain.team.SlackIdentity teamSlackIdentity = null;
            if (teamEntity.getSlackDetails() != null) {
                teamSlackIdentity = new za.co.absa.subatomic.domain.team.SlackIdentity(
                        teamEntity.getSlackDetails().getTeamChannel());
            }
            teamList.add(new Team(
                    teamEntity.getTeamId(),
                    teamEntity.getName(),
                    teamSlackIdentity));
        }

        TeamEntity owningTeam = projectEntity.getOwningTeam();

        za.co.absa.subatomic.domain.team.SlackIdentity owningTeamSlackIdentity = null;

        if (teamMemberEntity.getSlackDetails() != null) {
            owningTeamSlackIdentity = new za.co.absa.subatomic.domain.team.SlackIdentity(
                    owningTeam.getSlackDetails().getTeamChannel());
        }

        ApplicationEnvironmentRequestedWithDetails environmentRequested = new ApplicationEnvironmentRequestedWithDetails(
                ApplicationCreated.builder()
                        .applicationId(applicationEntity.getApplicationId())
                        .name(applicationEntity.getName())
                        .description(applicationEntity.getDescription())
                        .applicationType(applicationEntity.getApplicationType())
                        .build(),
                ProjectCreated.builder()
                        .projectId(projectEntity.getProjectId())
                        .name(projectEntity.getName())
                        .description(projectEntity.getDescription())
                        .build(),
                BitbucketGitRepository.builder()
                        .bitbucketId(event.getBitbucketGitRepository()
                                .getBitbucketId())
                        .slug(event.getBitbucketGitRepository().getSlug())
                        .name(event.getBitbucketGitRepository().getName())
                        .repoUrl(event.getBitbucketGitRepository().getRepoUrl())
                        .remoteUrl(event.getBitbucketGitRepository()
                                .getRemoteUrl())
                        .build(),
                new BitbucketProject(
                        projectEntity.getBitbucketProject().getId().toString(),
                        projectEntity.getBitbucketProject().getKey(),
                        projectEntity.getBitbucketProject().getName(),
                        projectEntity.getBitbucketProject().getDescription(),
                        projectEntity.getBitbucketProject().getUrl()),
                new Team(owningTeam.getTeamId(), owningTeam.getName(),
                        owningTeamSlackIdentity),
                teamList,
                new CreatedBy(
                        teamMemberEntity.getMemberId(),
                        teamMemberEntity.getFirstName(),
                        teamMemberEntity.getLastName(),
                        teamMemberEntity.getEmail(),
                        slackIdentity));

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties.getApplicationCreatedEventUrl(),
                environmentRequested,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @Value
    private class ApplicationEnvironmentRequestedWithDetails {

        private ApplicationCreated application;

        private ProjectCreated project;

        private BitbucketGitRepository bitbucketRepository;

        private BitbucketProject bitbucketProject;

        private Team owningTeam;

        private List<Team> teams;

        private CreatedBy requestedBy;
    }

    @Value
    private class Team {

        private String teamId;

        private String name;

        private za.co.absa.subatomic.domain.team.SlackIdentity slackIdentity;
    }

    @Value
    private class CreatedBy {

        private String memberId;

        private String firstName;

        private String lastName;

        private String email;

        private za.co.absa.subatomic.domain.member.SlackIdentity slackIdentity;
    }
}
