package za.co.absa.subatomic.infrastructure.team;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.domain.member.SlackIdentity;
import za.co.absa.subatomic.domain.team.DevOpsEnvironmentRequested;
import za.co.absa.subatomic.domain.team.MembershipRequestCreated;
import za.co.absa.subatomic.domain.team.MembershipRequestUpdated;
import za.co.absa.subatomic.domain.team.TeamCreated;
import za.co.absa.subatomic.infrastructure.AtomistConfiguration;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamRepository;

@Component
@Slf4j
public class TeamAutomationHandler {

    private TeamMemberRepository teamMemberRepository;

    private TeamRepository teamRepository;

    private RestTemplate restTemplate;

    private AtomistConfiguration atomistConfiguration;

    public TeamAutomationHandler(TeamMemberRepository teamMemberRepository,
                                 TeamRepository teamRepository, RestTemplate restTemplate, AtomistConfiguration atomistConfiguration) {
        this.teamMemberRepository = teamMemberRepository;
        this.teamRepository = teamRepository;
        this.restTemplate = restTemplate;
        this.atomistConfiguration = atomistConfiguration;
    }

    @EventHandler
    public void on(TeamCreated event) {
        log.info("A team was created, sending event to Atomist...{}", event);

        TeamMemberEntity teamMemberEntity = teamMemberRepository
                .findByMemberId(event.getCreatedBy().getTeamMemberId());

        SlackIdentity slackIdentity = null;
        if (teamMemberEntity.getSlackDetails() != null) {
            slackIdentity = new SlackIdentity(
                    teamMemberEntity.getSlackDetails()
                            .getScreenName(),
                    teamMemberEntity.getSlackDetails()
                            .getUserId());
        }

        TeamCreatedWithDetails newTeam = new TeamCreatedWithDetails(event,
                new TeamMember(
                        teamMemberEntity.getMemberId(),
                        teamMemberEntity.getDomainUsername(),
                        teamMemberEntity.getFirstName(),
                        teamMemberEntity.getLastName(),
                        teamMemberEntity.getEmail(),
                        slackIdentity));

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfiguration.getTeamCreatedEventUrl(),
                newTeam,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @EventHandler
    public void on(DevOpsEnvironmentRequested event) {
        log.info(
                "A team DevOps environment was requested, sending event to Atomist...");

        TeamEntity teamEntity = teamRepository.findByTeamId(event.getTeamId());
        TeamMemberEntity teamMemberEntity = teamMemberRepository
                .findByMemberId(event.getRequestedBy().getTeamMemberId());

        DevOpsEnvironmentRequestedWithDetails newDevOpsEnvironmentRequested = new DevOpsEnvironmentRequestedWithDetails(
                new Team(
                        teamEntity.getTeamId(),
                        teamEntity.getName(),
                        new za.co.absa.subatomic.domain.team.SlackIdentity(
                                teamEntity.getSlackDetails().getTeamChannel())),
                new DevOpsTeamMember(
                        null,
                        teamMemberEntity.getFirstName(),
                        new SlackIdentity(
                                teamMemberEntity.getSlackDetails()
                                        .getScreenName(),
                                teamMemberEntity.getSlackDetails()
                                        .getUserId())));
        newDevOpsEnvironmentRequested.getTeam().getOwners().addAll(
                teamEntity.getOwners().stream()
                        .map(memberEntity -> new DevOpsTeamMember(
                                memberEntity.getDomainUsername(),
                                memberEntity.getFirstName(),
                                new SlackIdentity(
                                        memberEntity.getSlackDetails()
                                                .getScreenName(),
                                        memberEntity.getSlackDetails()
                                                .getUserId())))
                        .collect(Collectors.toList()));
        newDevOpsEnvironmentRequested.getTeam().getMembers().addAll(
                teamEntity.getMembers().stream()
                        .map(memberEntity -> new DevOpsTeamMember(
                                memberEntity.getDomainUsername(),
                                memberEntity.getFirstName(),
                                new SlackIdentity(
                                        memberEntity.getSlackDetails()
                                                .getScreenName(),
                                        memberEntity.getSlackDetails()
                                                .getUserId())))
                        .collect(Collectors.toList()));

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfiguration.getDevOpsEnvironmentRequestedEventUrl(),
                newDevOpsEnvironmentRequested,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @EventHandler
    public void on(MembershipRequestCreated event) {
        log.info(
                "Membership to a team has been requested, sending event to Atomist...");

        TeamMemberEntity requestedBy = teamMemberRepository.findByMemberId(
                event.getMembershipRequest().getRequestedBy()
                        .getTeamMemberId());
        za.co.absa.subatomic.domain.member.SlackIdentity requestedBySlackIdentity = null;
        if (requestedBy.getSlackDetails() != null) {
            requestedBySlackIdentity = new za.co.absa.subatomic.domain.member.SlackIdentity(
                    requestedBy.getSlackDetails()
                            .getScreenName(),
                    requestedBy.getSlackDetails()
                            .getUserId());
        }

        TeamEntity teamEntity = teamRepository.findByTeamId(event.getTeamId());
        za.co.absa.subatomic.domain.team.SlackIdentity teamEntitySlackIdentity = null;
        if (teamEntity.getSlackDetails() != null) {
            teamEntitySlackIdentity = new za.co.absa.subatomic.domain.team.SlackIdentity(
                    teamEntity.getSlackDetails()
                            .getTeamChannel());
        }

        Team team = new Team(teamEntity.getTeamId(), teamEntity.getName(),
                teamEntitySlackIdentity);
        TeamMember member = new TeamMember(requestedBy.getMemberId(),
                requestedBy.getDomainUsername(), requestedBy.getFirstName(),
                requestedBy.getLastName(), requestedBy.getEmail(),
                requestedBySlackIdentity);

        MembershipRequestCreatedWithDetails newRequest = new MembershipRequestCreatedWithDetails(
                event,
                team,
                member);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfiguration.getMembershipRequestCreatedEventUrl(),
                newRequest,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @EventHandler
    public void on(MembershipRequestUpdated event) {
        log.info(
                "A team membership request has been updated, sending event to Atomist...");

        TeamMemberEntity requestedBy = teamMemberRepository.findByMemberId(
                event.getMembershipRequest().getRequestedBy()
                        .getTeamMemberId());
        TeamMemberEntity approvedBy = teamMemberRepository.findByMemberId(
                event.getMembershipRequest().getApprovedBy()
                        .getTeamMemberId());
        za.co.absa.subatomic.domain.member.SlackIdentity requestedBySlackIdentity = null;
        if (requestedBy.getSlackDetails() != null) {
            requestedBySlackIdentity = new za.co.absa.subatomic.domain.member.SlackIdentity(
                    requestedBy.getSlackDetails()
                            .getScreenName(),
                    requestedBy.getSlackDetails()
                            .getUserId());
        }

        TeamEntity teamEntity = teamRepository.findByTeamId(event.getTeamId());
        za.co.absa.subatomic.domain.team.SlackIdentity teamEntitySlackIdentity = null;
        if (teamEntity.getSlackDetails() != null) {
            teamEntitySlackIdentity = new za.co.absa.subatomic.domain.team.SlackIdentity(
                    teamEntity.getSlackDetails()
                            .getTeamChannel());
        }

        Team team = new Team(teamEntity.getTeamId(), teamEntity.getName(),
                teamEntitySlackIdentity);
        TeamMember requestedByMember = new TeamMember(requestedBy.getMemberId(),
                requestedBy.getDomainUsername(), requestedBy.getFirstName(),
                requestedBy.getLastName(), requestedBy.getEmail(),
                requestedBySlackIdentity);
        TeamMember approvedByMember = new TeamMember(approvedBy.getMemberId(),
                requestedBy.getDomainUsername(), requestedBy.getFirstName(),
                requestedBy.getLastName(), requestedBy.getEmail(),
                requestedBySlackIdentity);

        MembershipRequestUpdatedWithDetails newRequest = new MembershipRequestUpdatedWithDetails(
                event,
                team,
                requestedByMember,
                approvedByMember
                );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://webhook.atomist.com/atomist/teams/T8RGCS6T0/ingestion/TeamCreatedEvent/cfd69c30-5ba0-453a-bf61-2314af439428",
                newRequest,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @Value
    private class TeamCreatedWithDetails {

        private TeamCreated team;

        private TeamMember createdBy;
    }

    @Value
    private class DevOpsEnvironmentRequestedWithDetails {

        private Team team;

        private DevOpsTeamMember requestedBy;
    }

    @Value
    private class Team {

        private String teamId;

        private String name;

        private za.co.absa.subatomic.domain.team.SlackIdentity slackIdentity;

        private final List<DevOpsTeamMember> owners = new ArrayList<>();

        private final List<DevOpsTeamMember> members = new ArrayList<>();
    }

    @Value
    private class TeamMember {

        private String memberId;

        private String domainUsername;

        private String firstName;

        private String lastName;

        private String email;

        private SlackIdentity slackIdentity;
    }

    @Value
    private class DevOpsTeamMember {

        private String domainUsername;

        private String firstName;

        private SlackIdentity slackIdentity;
    }

    @Value
    private class MembershipRequestCreatedWithDetails {
        MembershipRequestCreated membershipRequestCreated;

        Team team;

        TeamMember requestedBy;
    }

    @Value
    private class MembershipRequestUpdatedWithDetails {
        MembershipRequestUpdated membershipRequestUpdated;

        Team team;

        TeamMember requestedBy;

        TeamMember approvedBy;
    }
}
