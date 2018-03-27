package za.co.absa.subatomic.infrastructure.team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.domain.member.SlackIdentity;
import za.co.absa.subatomic.domain.team.DevOpsEnvironmentRequested;
import za.co.absa.subatomic.domain.team.MembershipRequestCreated;
import za.co.absa.subatomic.domain.team.TeamCreated;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.domain.team.TeamMembersAdded;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
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

    private AtomistConfigurationProperties atomistConfigurationProperties;

    public TeamAutomationHandler(TeamMemberRepository teamMemberRepository,
            TeamRepository teamRepository, RestTemplate restTemplate,
            AtomistConfigurationProperties atomistConfigurationProperties) {
        this.teamMemberRepository = teamMemberRepository;
        this.teamRepository = teamRepository;
        this.restTemplate = restTemplate;
        this.atomistConfigurationProperties = atomistConfigurationProperties;
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
                atomistConfigurationProperties.getTeamCreatedEventUrl(),
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
                atomistConfigurationProperties
                        .getDevOpsEnvironmentRequestedEventUrl(),
                newDevOpsEnvironmentRequested,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @EventHandler
    public void on(MembershipRequestCreated event) {
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
                event.getMembershipRequest().getMembershipRequestId(),
                team,
                member);

        log.info(
                "A team membership request has been updated, sending event to Atomist...{}",
                newRequest);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getMembershipRequestCreatedEventUrl(),
                newRequest,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    @EventHandler
    @Transactional
    void on(TeamMembersAdded event) {
        TeamEntity teamEntity = teamRepository.findByTeamId(event.getTeamId());
        za.co.absa.subatomic.domain.team.SlackIdentity teamEntitySlackIdentity = null;
        if (teamEntity.getSlackDetails() != null) {
            teamEntitySlackIdentity = new za.co.absa.subatomic.domain.team.SlackIdentity(
                    teamEntity.getSlackDetails()
                            .getTeamChannel());
        }

        Team team = new Team(teamEntity.getTeamId(), teamEntity.getName(),
                teamEntitySlackIdentity);

        List<TeamMember> owners = teamMemberIdCollectionToTeamMemberList(
                event.getOwners());
        List<TeamMember> members = teamMemberIdCollectionToTeamMemberList(
                event.getTeamMembers());

        MembersAddedToTeamWithDetails membersAddedEvent = new MembersAddedToTeamWithDetails(
                team, owners, members);

        log.info(
                "New members have been added to a team, sending event to Atomist...{}",
                membersAddedEvent);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getMembersAddedToTeamEventUrl(),
                membersAddedEvent,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    private List<TeamMember> teamMemberIdCollectionToTeamMemberList(
            Collection<TeamMemberId> teamMemberIdList) {
        List<TeamMember> members = new ArrayList<>();
        for (TeamMemberId member : teamMemberIdList) {
            TeamMemberEntity memberEntity = teamMemberRepository.findByMemberId(
                    member.getTeamMemberId());
            za.co.absa.subatomic.domain.member.SlackIdentity memberSlackIdentity = null;
            if (memberEntity.getSlackDetails() != null) {
                memberSlackIdentity = new za.co.absa.subatomic.domain.member.SlackIdentity(
                        memberEntity.getSlackDetails()
                                .getScreenName(),
                        memberEntity.getSlackDetails()
                                .getUserId());
            }
            members.add(new TeamMember(memberEntity.getMemberId(),
                    memberEntity.getDomainUsername(),
                    memberEntity.getFirstName(),
                    memberEntity.getLastName(),
                    memberEntity.getEmail(),
                    memberSlackIdentity));
        }
        return members;
    }

    @Value
    class MembersAddedToTeamWithDetails {
        private Team team;

        private List<TeamMember> owners;

        private List<TeamMember> members;
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

        String membershipRequestId;

        Team team;

        TeamMember requestedBy;
    }

}
