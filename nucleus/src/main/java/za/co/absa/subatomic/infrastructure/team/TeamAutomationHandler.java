package za.co.absa.subatomic.infrastructure.team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.domain.member.TeamMemberSlackIdentity;
import za.co.absa.subatomic.domain.team.TeamCreated;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.MembershipRequestEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@Component
@Slf4j
public class TeamAutomationHandler {

    private RestTemplate restTemplate;

    private AtomistConfigurationProperties atomistConfigurationProperties;

    public TeamAutomationHandler(RestTemplate restTemplate,
            AtomistConfigurationProperties atomistConfigurationProperties) {
        this.restTemplate = restTemplate;
        this.atomistConfigurationProperties = atomistConfigurationProperties;
    }

    public void createNewTeam(TeamEntity newTeamEntity) {
        log.info("A team was created, sending event to Atomist...{}",
                newTeamEntity);

        TeamMemberEntity teamMemberEntity = newTeamEntity.getCreatedBy();

        TeamMemberSlackIdentity teamMemberSlackIdentity = null;
        if (teamMemberEntity.getSlackDetails() != null) {
            teamMemberSlackIdentity = new TeamMemberSlackIdentity(
                    teamMemberEntity.getSlackDetails()
                            .getScreenName(),
                    teamMemberEntity.getSlackDetails()
                            .getUserId());
        }

        za.co.absa.subatomic.domain.team.SlackIdentity teamSlackIdentity = null;
        if (newTeamEntity.getSlackDetails() != null) {
            teamSlackIdentity = new za.co.absa.subatomic.domain.team.SlackIdentity(
                    newTeamEntity.getSlackDetails().getTeamChannel());
        }
        TeamCreated teamCreated = new TeamCreated(newTeamEntity.getTeamId(),
                newTeamEntity.getName(), newTeamEntity.getDescription(),
                new TeamMemberId(newTeamEntity.getCreatedBy().getMemberId()),
                teamSlackIdentity);

        TeamCreatedWithDetails newTeam = new TeamCreatedWithDetails(teamCreated,
                new TeamMember(
                        teamMemberEntity.getMemberId(),
                        teamMemberEntity.getDomainUsername(),
                        teamMemberEntity.getFirstName(),
                        teamMemberEntity.getLastName(),
                        teamMemberEntity.getEmail(),
                        teamMemberSlackIdentity));

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties.getTeamCreatedEventUrl(),
                newTeam,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    public void devOpsEnvironmentRequested(TeamEntity teamEntity,
            TeamMemberEntity teamMemberEntity) {
        log.info("A team DevOps environment was requested, sending event to Atomist...");

        za.co.absa.subatomic.domain.team.SlackIdentity teamSlackIdentity = null;
        if (teamEntity.getSlackDetails() != null) {
            teamSlackIdentity = new za.co.absa.subatomic.domain.team.SlackIdentity(
                    teamEntity.getSlackDetails().getTeamChannel());
        }

        DevOpsEnvironmentRequestedWithDetails newDevOpsEnvironmentRequested = new DevOpsEnvironmentRequestedWithDetails(
                new Team(
                        teamEntity.getTeamId(),
                        teamEntity.getName(),
                        teamSlackIdentity),
                new DevOpsTeamMember(
                        null,
                        teamMemberEntity.getFirstName(),
                        new TeamMemberSlackIdentity(
                                teamMemberEntity.getSlackDetails()
                                        .getScreenName(),
                                teamMemberEntity.getSlackDetails()
                                        .getUserId())));
        newDevOpsEnvironmentRequested.getTeam().getOwners().addAll(
                teamEntity.getOwners().stream()
                        .map(memberEntity -> new DevOpsTeamMember(
                                memberEntity.getDomainUsername(),
                                memberEntity.getFirstName(),
                                new TeamMemberSlackIdentity(
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
                                new TeamMemberSlackIdentity(
                                        memberEntity.getSlackDetails()
                                                .getScreenName(),
                                        memberEntity.getSlackDetails()
                                                .getUserId())))
                        .collect(Collectors.toList()));

        log.info("Sending payload to atomist: {}",
                newDevOpsEnvironmentRequested);

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

    @Transactional(readOnly = true)
    public void membershipRequestCreated(
            TeamEntity teamEntity,
            MembershipRequestEntity membershipRequestEntity) {
        TeamMemberEntity requestedBy = membershipRequestEntity.getRequestedBy();
        TeamMemberSlackIdentity requestedBySlackIdentity = null;
        if (requestedBy.getSlackDetails() != null) {
            requestedBySlackIdentity = new TeamMemberSlackIdentity(
                    requestedBy.getSlackDetails()
                            .getScreenName(),
                    requestedBy.getSlackDetails()
                            .getUserId());
        }

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
                membershipRequestEntity.getMembershipRequestId(),
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

    public void teamMembersAdded(TeamEntity teamEntity,
            List<TeamMemberEntity> teamOwnersRequested,
            List<TeamMemberEntity> teamMembersRequested) {
        za.co.absa.subatomic.domain.team.SlackIdentity teamEntitySlackIdentity = null;
        if (teamEntity.getSlackDetails() != null) {
            teamEntitySlackIdentity = new za.co.absa.subatomic.domain.team.SlackIdentity(
                    teamEntity.getSlackDetails()
                            .getTeamChannel());
        }

        Team team = new Team(teamEntity.getTeamId(), teamEntity.getName(),
                teamEntitySlackIdentity);

        List<TeamMember> owners = teamMemberEntityCollectionToTeamMemberList(
                teamOwnersRequested);
        List<TeamMember> members = teamMemberEntityCollectionToTeamMemberList(
                teamMembersRequested);

        MembersAddedToTeamWithDetails membersAddedEvent = new MembersAddedToTeamWithDetails(
                team, owners, members);

        log.info("New members have been added to a team, sending event to Atomist...{}", membersAddedEvent);

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

    public void teamMemberRemoved(TeamEntity teamEntity,
                                  TeamMemberEntity memberEntity,
                                  TeamMemberEntity requesterEntity) {

        za.co.absa.subatomic.domain.team.SlackIdentity teamEntitySlackIdentity = null;

        if (teamEntity.getSlackDetails() != null) {
            teamEntitySlackIdentity = new za.co.absa.subatomic.domain.team.SlackIdentity(
                    teamEntity.getSlackDetails()
                            .getTeamChannel());
        }

        Team team = new Team(teamEntity.getTeamId(), teamEntity.getName(),
                teamEntitySlackIdentity);

        TeamMember memberRemoved = new TeamMember(
                memberEntity.getMemberId(),
                memberEntity.getDomainUsername(),
                memberEntity.getFirstName(),
                memberEntity.getLastName(),
                memberEntity.getEmail(),
                GetTeamMemberSlackIdentity(memberEntity));

        TeamMember memberRequested = new TeamMember(
                requesterEntity.getMemberId(),
                requesterEntity.getDomainUsername(),
                requesterEntity.getFirstName(),
                requesterEntity.getLastName(),
                requesterEntity.getEmail(),
                GetTeamMemberSlackIdentity(requesterEntity));

        // Build the ingestable object
        MemberRemovedFromTeam ingestableObject = new MemberRemovedFromTeam(
                team, memberRemoved, memberRequested);

        log.info("A member has been removed from a team, sending event to Atomist for ingestion", ingestableObject);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getMembersRemovedFromTeamEventUrl(),
                ingestableObject,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Atomist has ingested event memberRemovedEvent successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    private TeamMemberSlackIdentity GetTeamMemberSlackIdentity(TeamMemberEntity memberEntity)
    {
        TeamMemberSlackIdentity memberSlackIdentity = null;

        if (memberEntity.getSlackDetails() != null) {
            memberSlackIdentity = new TeamMemberSlackIdentity(
                    memberEntity.getSlackDetails()
                            .getScreenName(),
                    memberEntity.getSlackDetails()
                            .getUserId());
        }

        return memberSlackIdentity;
    }

    private List<TeamMember> teamMemberEntityCollectionToTeamMemberList(
            Collection<TeamMemberEntity> teamMemberEntities) {
        List<TeamMember> members = new ArrayList<>();
        for (TeamMemberEntity memberEntity : teamMemberEntities) {

            TeamMemberSlackIdentity memberSlackIdentity = GetTeamMemberSlackIdentity(memberEntity);

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
    private class MembersAddedToTeamWithDetails {
        private Team team;

        private List<TeamMember> owners;

        private List<TeamMember> members;
    }

    @Value
    private class MemberRemovedFromTeam {
        private Team team;

        private TeamMember memberRemoved;

        private TeamMember memberRequester;
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

        private TeamMemberSlackIdentity slackIdentity;
    }

    @Value
    private class DevOpsTeamMember {

        private String domainUsername;

        private String firstName;

        private TeamMemberSlackIdentity slackIdentity;
    }

    @Value
    private class MembershipRequestCreatedWithDetails {

        String membershipRequestId;

        Team team;

        TeamMember requestedBy;
    }

}
