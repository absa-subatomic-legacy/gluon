package za.co.absa.subatomic.infrastructure.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.adapter.application.rest.BitbucketRepository;
import za.co.absa.subatomic.infrastructure.atomist.resource.AtomistApplication;
import za.co.absa.subatomic.domain.member.TeamMemberSlackIdentity;
import za.co.absa.subatomic.domain.project.BitbucketProject;
import za.co.absa.subatomic.domain.team.TeamSlackIdentity;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationEntity;
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationRepository;
import za.co.absa.subatomic.infrastructure.atomist.resource.project.AtomistProject;
import za.co.absa.subatomic.infrastructure.atomist.resource.project.AtomistProjectMapper;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@Component
@Slf4j
public class ApplicationAutomationHandler {

    private RestTemplate restTemplate;

    private ApplicationRepository applicationRepository;

    private AtomistConfigurationProperties atomistConfigurationProperties;

    public ApplicationAutomationHandler(RestTemplate restTemplate,
            ApplicationRepository applicationRepository,
            AtomistConfigurationProperties atomistConfigurationProperties) {
        this.restTemplate = restTemplate;
        this.applicationRepository = applicationRepository;
        this.atomistConfigurationProperties = atomistConfigurationProperties;
    }

    public void applicationCreated(ApplicationEntity newApplication,
            boolean configurationRequested) {
        log.info(
                "An application was created for project [{}], sending event to Atomist: {}",
                newApplication.getProject().getProjectId(), newApplication);

        ApplicationEntity applicationEntity = applicationRepository
                .findByApplicationId(
                        newApplication.getApplicationId());

        ProjectEntity projectEntity = newApplication.getProject();

        TeamMemberEntity teamMemberEntity = newApplication.getCreatedBy();

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
                AtomistApplication.builder()
                        .applicationId(applicationEntity.getApplicationId())
                        .name(applicationEntity.getName())
                        .description(applicationEntity.getDescription())
                        .applicationType(applicationEntity.getApplicationType())
                        .build(),
                new AtomistProjectMapper().createAtomistProject(projectEntity),
                BitbucketRepository.builder()
                        .bitbucketId(newApplication.getBitbucketRepository()
                                .getBitbucketId())
                        .slug(newApplication.getBitbucketRepository().getSlug())
                        .name(newApplication.getBitbucketRepository().getName())
                        .repoUrl(newApplication.getBitbucketRepository()
                                .getRepoUrl())
                        .remoteUrl(newApplication.getBitbucketRepository()
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
                configurationRequested);

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

        private AtomistApplication application;

        private AtomistProject project;

        private BitbucketRepository bitbucketRepository;

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
