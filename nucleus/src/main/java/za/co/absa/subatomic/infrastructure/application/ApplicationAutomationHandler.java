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
import za.co.absa.subatomic.domain.application.BitbucketGitRepository;
import za.co.absa.subatomic.domain.member.TeamMemberSlackIdentity;
import za.co.absa.subatomic.domain.project.BitbucketProject;
import za.co.absa.subatomic.domain.team.TeamSlackIdentity;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationEntity;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationRepository;
import za.co.absa.subatomic.infrastructure.atomist.resource.project.AtomistProject;
import za.co.absa.subatomic.infrastructure.atomist.resource.project.AtomistProjectMapper;
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
    void on(ApplicationCreated event) {
        log.info(
                "An application was created for project [{}], sending event to Atomist: {}",
                event.getProjectId().getProjectId(), event);

        ApplicationEntity applicationEntity = applicationRepository
                .findByApplicationId(
                        event.getApplicationId());

        ProjectEntity projectEntity = projectRepository
                .findByProjectId(event.getProjectId().getProjectId());

        TeamMemberEntity teamMemberEntity = teamMemberRepository
                .findByMemberId(event.getCreatedBy().getTeamMemberId());

        TeamMemberSlackIdentity teamMemberSlackIdentity = null;
        if (teamMemberEntity.getSlackDetails() != null) {
            teamMemberSlackIdentity = new TeamMemberSlackIdentity(
                    teamMemberEntity.getSlackDetails()
                            .getScreenName(),
                    teamMemberEntity.getSlackDetails()
                            .getUserId());
        }

        List<Team> teamList = new ArrayList<>();
        for (TeamEntity teamEntity : projectEntity.getTeams()) {
            TeamSlackIdentity teamSlackIdentity = null;
            if (teamEntity.getSlackDetails() != null) {
                teamSlackIdentity = new TeamSlackIdentity(
                        teamEntity.getSlackDetails().getTeamChannel());
            }
            teamList.add(new Team(
                    teamEntity.getTeamId(),
                    teamEntity.getName(),
                    teamEntity.getOpenShiftCloud(),
                    teamSlackIdentity));
        }

        TeamEntity owningTeam = projectEntity.getOwningTeam();

        TeamSlackIdentity owningTeamSlackIdentity = null;

        if (owningTeam.getSlackDetails() != null) {
            owningTeamSlackIdentity = new TeamSlackIdentity(
                    owningTeam.getSlackDetails().getTeamChannel());
        }

        ApplicationCreatedWithDetails applicationCreated = new ApplicationCreatedWithDetails(
                ApplicationCreated.builder()
                        .applicationId(applicationEntity.getApplicationId())
                        .name(applicationEntity.getName())
                        .description(applicationEntity.getDescription())
                        .applicationType(applicationEntity.getApplicationType())
                        .build(),
                new AtomistProjectMapper().createAtomistProject(projectEntity),
                BitbucketGitRepository.builder()
                        .bitbucketId(event.getBitbucketRepository()
                                .getBitbucketId())
                        .slug(event.getBitbucketRepository().getSlug())
                        .name(event.getBitbucketRepository().getName())
                        .repoUrl(event.getBitbucketRepository().getRepoUrl())
                        .remoteUrl(event.getBitbucketRepository()
                                .getRemoteUrl())
                        .build(),
                new BitbucketProject(
                        projectEntity.getBitbucketProject().getId().toString(),
                        projectEntity.getBitbucketProject().getKey(),
                        projectEntity.getBitbucketProject().getName(),
                        projectEntity.getBitbucketProject().getDescription(),
                        projectEntity.getBitbucketProject().getUrl()),
                new Team(owningTeam.getTeamId(), owningTeam.getName(),
                        owningTeam.getOpenShiftCloud(),
                        owningTeamSlackIdentity),
                teamList,
                new CreatedBy(
                        teamMemberEntity.getMemberId(),
                        teamMemberEntity.getFirstName(),
                        teamMemberEntity.getLastName(),
                        teamMemberEntity.getEmail(),
                        teamMemberSlackIdentity),
                event.getRequestConfiguration());

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties.getApplicationCreatedEventUrl(),
                applicationCreated,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @Value
    private class ApplicationCreatedWithDetails {

        private ApplicationCreated application;

        private AtomistProject project;

        private BitbucketGitRepository bitbucketRepository;

        private BitbucketProject bitbucketProject;

        private Team owningTeam;

        private List<Team> teams;

        private CreatedBy requestedBy;

        private Boolean requestConfiguration;
    }

    @Value
    private class Team {

        private String teamId;

        private String name;

        private String openShiftCloud;

        private TeamSlackIdentity slackIdentity;
    }

    @Value
    private class CreatedBy {

        private String memberId;

        private String firstName;

        private String lastName;

        private String email;

        private TeamMemberSlackIdentity slackIdentity;
    }
}
