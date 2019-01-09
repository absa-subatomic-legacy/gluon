package za.co.absa.subatomic.infrastructure.team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.domain.member.TeamMemberSlackIdentity;
import za.co.absa.subatomic.domain.team.TeamCreated;
import za.co.absa.subatomic.domain.team.TeamMemberId;
import za.co.absa.subatomic.domain.team.TeamSlackIdentity;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
import za.co.absa.subatomic.infrastructure.atomist.resource.AtomistMemberBase;
import za.co.absa.subatomic.infrastructure.atomist.resource.team.AtomistTeam;
import za.co.absa.subatomic.infrastructure.atomist.resource.team.AtomistTeamMapper;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.MembershipRequestEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@Component
@Slf4j
public class TeamAutomationHandler {

    private RestTemplate restTemplate;

    private AtomistConfigurationProperties atomistConfigurationProperties;

    private AtomistTeamMapper atomistTeamMapper = new AtomistTeamMapper();

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

        TeamSlackIdentity teamSlackIdentity = null;
        if (newTeamEntity.getSlackDetails() != null) {
            teamSlackIdentity = new TeamSlackIdentity(
                    newTeamEntity.getSlackDetails().getTeamChannel());
        }
        TeamCreated teamCreated = new TeamCreated(newTeamEntity.getTeamId(),
                newTeamEntity.getName(), newTeamEntity.getDescription(),
                new TeamMemberId(newTeamEntity.getCreatedBy().getMemberId()),
                teamSlackIdentity);

        TeamCreatedWithDetails newTeam = new TeamCreatedWithDetails(teamCreated,
                new AtomistMemberBase(
                        teamMemberEntity.getFirstName(),
                        teamMemberEntity.getDomainUsername(),
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
        log.info(
                "A team DevOps environment was requested, sending event to Atomist...");

        DevOpsEnvironmentRequestedWithDetails newDevOpsEnvironmentRequested = new DevOpsEnvironmentRequestedWithDetails(
                atomistTeamMapper.createAtomistTeam(teamEntity),
                new AtomistMemberBase(
                        null,
                        teamMemberEntity.getFirstName(),
                        new TeamMemberSlackIdentity(
                                teamMemberEntity.getSlackDetails()
                                        .getScreenName(),
                                teamMemberEntity.getSlackDetails()
                                        .getUserId())));

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

    public void teamOpenShiftCloudUpdated(TeamEntity teamEntity,
            TeamMemberEntity teamMemberEntity) {
        log.info(
                "A team OpenShift cloud has been updated, sending event to Atomist...");

        TeamOpenShiftCloudMigrationEvent teamOpenShiftCloudMigrationEvent = new TeamOpenShiftCloudMigrationEvent(
                atomistTeamMapper.createAtomistTeam(teamEntity),
                new AtomistMemberBase(
                        null,
                        teamMemberEntity.getFirstName(),
                        new TeamMemberSlackIdentity(
                                teamMemberEntity.getSlackDetails()
                                        .getScreenName(),
                                teamMemberEntity.getSlackDetails()
                                        .getUserId())));

        log.info("Sending payload to atomist: {}",
                teamOpenShiftCloudMigrationEvent);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getTeamOpenShiftCloudMigratedEventUrl(),
                teamOpenShiftCloudMigrationEvent,
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

        TeamSlackIdentity teamEntitySlackIdentity = null;
        if (teamEntity.getSlackDetails() != null) {
            teamEntitySlackIdentity = new TeamSlackIdentity(
                    teamEntity.getSlackDetails()
                            .getTeamChannel());
        }

        AtomistTeam team = new AtomistTeam(teamEntity.getTeamId(),
                teamEntity.getName(),
                teamEntity.getOpenShiftCloud(),
                teamEntitySlackIdentity);
        AtomistMemberBase member = new AtomistMemberBase(
                requestedBy.getFirstName(),
                requestedBy.getDomainUsername(),
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
        TeamSlackIdentity teamEntitySlackIdentity = null;
        if (teamEntity.getSlackDetails() != null) {
            teamEntitySlackIdentity = new TeamSlackIdentity(
                    teamEntity.getSlackDetails()
                            .getTeamChannel());
        }

        AtomistTeam team = new AtomistTeam(teamEntity.getTeamId(),
                teamEntity.getName(),
                teamEntity.getOpenShiftCloud(),
                teamEntitySlackIdentity);

        List<AtomistMemberBase> owners = teamMemberEntityCollectionToTeamMemberList(
                teamOwnersRequested);
        List<AtomistMemberBase> members = teamMemberEntityCollectionToTeamMemberList(
                teamMembersRequested);

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

    public void teamMemberRemoved(TeamEntity teamEntity,
            TeamMemberEntity memberEntity,
            TeamMemberEntity requesterEntity) {

        TeamSlackIdentity teamEntitySlackIdentity = null;

        if (teamEntity.getSlackDetails() != null) {
            teamEntitySlackIdentity = new TeamSlackIdentity(
                    teamEntity.getSlackDetails()
                            .getTeamChannel());
        }

        AtomistTeam team = new AtomistTeam(teamEntity.getTeamId(),
                teamEntity.getName(),
                teamEntity.getOpenShiftCloud(),
                teamEntitySlackIdentity);

        AtomistMemberBase memberRemoved = new AtomistMemberBase(
                memberEntity.getFirstName(),
                memberEntity.getDomainUsername(),
                getTeamMemberSlackIdentity(memberEntity));

        AtomistMemberBase memberRequested = new AtomistMemberBase(
                requesterEntity.getFirstName(),
                requesterEntity.getDomainUsername(),
                getTeamMemberSlackIdentity(requesterEntity));

        // Build the ingestable object
        MemberRemovedFromTeam ingestableObject = new MemberRemovedFromTeam(
                team, memberRemoved, memberRequested);

        log.info(
                "A member has been removed from a team, sending event to Atomist for ingestion",
                ingestableObject);

        ResponseEntity<String> response = restTemplate.postForEntity(
                atomistConfigurationProperties
                        .getMembersRemovedFromTeamEventUrl(),
                ingestableObject,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info(
                    "Atomist has ingested event memberRemovedEvent successfully: {} -> {}",
                    response.getHeaders(), response.getBody());
        }
    }

    private TeamMemberSlackIdentity getTeamMemberSlackIdentity(
            TeamMemberEntity memberEntity) {
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

    private List<AtomistMemberBase> teamMemberEntityCollectionToTeamMemberList(
            Collection<TeamMemberEntity> teamMemberEntities) {
        List<AtomistMemberBase> members = new ArrayList<>();
        for (TeamMemberEntity memberEntity : teamMemberEntities) {

            TeamMemberSlackIdentity memberSlackIdentity = getTeamMemberSlackIdentity(
                    memberEntity);

            members.add(new AtomistMemberBase(
                    memberEntity.getFirstName(),
                    memberEntity.getDomainUsername(),
                    memberSlackIdentity));
        }
        return members;
    }

    @Value
    private class MembersAddedToTeamWithDetails {
        private AtomistTeam team;

        private List<AtomistMemberBase> owners;

        private List<AtomistMemberBase> members;
    }

    @Value
    private class MemberRemovedFromTeam {
        private AtomistTeam team;

        private AtomistMemberBase memberRemoved;

        private AtomistMemberBase memberRequester;
    }

    @Value
    private class TeamCreatedWithDetails {

        private TeamCreated team;

        private AtomistMemberBase createdBy;
    }

    @Value
    private class DevOpsEnvironmentRequestedWithDetails {

        private AtomistTeam team;

        private AtomistMemberBase requestedBy;
    }

    @Value
    private class TeamOpenShiftCloudMigrationEvent {

        private AtomistTeam team;

        private AtomistMemberBase requestedBy;
    }

    @Value
    private class MembershipRequestCreatedWithDetails {

        String membershipRequestId;

        AtomistTeam team;

        AtomistMemberBase requestedBy;
    }

}
