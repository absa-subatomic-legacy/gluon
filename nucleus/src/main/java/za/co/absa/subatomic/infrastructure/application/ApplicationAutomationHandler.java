package za.co.absa.subatomic.infrastructure.application;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import za.co.absa.subatomic.domain.application.ApplicationCreated;
import za.co.absa.subatomic.domain.application.ApplicationEnvironmentRequested;
import za.co.absa.subatomic.domain.application.BitbucketGitRepository;
import za.co.absa.subatomic.domain.member.SlackIdentity;
import za.co.absa.subatomic.domain.project.BitbucketProject;
import za.co.absa.subatomic.domain.project.ProjectCreated;
import za.co.absa.subatomic.infrastructure.AtomistConfiguration;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationEntity;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationRepository;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ApplicationAutomationHandler {

    private RestTemplate restTemplate;

    private ApplicationRepository applicationRepository;

    private ProjectRepository projectRepository;

    private TeamMemberRepository teamMemberRepository;
    private AtomistConfiguration atomistConfiguration;

    public ApplicationAutomationHandler(RestTemplate restTemplate,
                                        ApplicationRepository applicationRepository,
                                        ProjectRepository projectRepository,
                                        TeamMemberRepository teamMemberRepository,
                                        AtomistConfiguration atomistConfiguration) {
        this.restTemplate = restTemplate;
        this.applicationRepository = applicationRepository;
        this.projectRepository = projectRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.atomistConfiguration = atomistConfiguration;
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

        ApplicationEnvironmentRequestedWithDetails environmentRequested = new ApplicationEnvironmentRequestedWithDetails(
                ApplicationCreated.builder()
                        .applicationId(applicationEntity.getApplicationId())
                        .name(applicationEntity.getName())
                        .description(applicationEntity.getDescription())
                        .build(),
                ProjectCreated.builder()
                        .projectId(projectEntity.getProjectId())
                        .name(projectEntity.getName())
                        .description(projectEntity.getDescription())
                        .build(),
                BitbucketGitRepository.builder()
                        .name(event.getBitbucketGitRepository().getName())
                        .repoUrl(event.getBitbucketGitRepository()
                                .getRepoUrl())
                        .build(),
                new BitbucketProject(
                        projectEntity.getBitbucketProject().getId().toString(),
                        projectEntity.getBitbucketProject().getKey(),
                        projectEntity.getBitbucketProject().getName(),
                        projectEntity.getBitbucketProject().getDescription(),
                        projectEntity.getBitbucketProject().getUrl()),
                projectEntity.getTeams().stream()
                        .map(teamEntity -> new Team(
                                teamEntity.getTeamId(),
                                teamEntity.getName(),
                                new za.co.absa.subatomic.domain.team.SlackIdentity(
                                        teamEntity.getSlackDetails()
                                                .getTeamChannel())))
                        .collect(Collectors.toList()),
                new CreatedBy(
                        teamMemberEntity.getMemberId(),
                        teamMemberEntity.getFirstName(),
                        teamMemberEntity.getLastName(),
                        teamMemberEntity.getEmail(),
                        slackIdentity));

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfiguration.getApplicationCreatedEventUrl(),
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
