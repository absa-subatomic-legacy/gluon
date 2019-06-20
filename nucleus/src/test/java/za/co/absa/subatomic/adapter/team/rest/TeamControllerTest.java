package za.co.absa.subatomic.adapter.team.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import za.co.absa.subatomic.adapter.member.rest.Slack;
import za.co.absa.subatomic.adapter.member.rest.TeamMemberController;
import za.co.absa.subatomic.adapter.member.rest.TeamMemberResource;
import za.co.absa.subatomic.adapter.metadata.rest.MetadataEntryResource;
import za.co.absa.subatomic.adapter.metadata.rest.MetadataResource;
import za.co.absa.subatomic.domain.member.TeamMemberSlackIdentity;
import za.co.absa.subatomic.domain.team.TeamSlackIdentity;
import za.co.absa.subatomic.infrastructure.AtomistConfigurationProperties;
import za.co.absa.subatomic.infrastructure.atomist.resource.AtomistMemberBase;
import za.co.absa.subatomic.infrastructure.atomist.resource.team.AtomistTeam;
import za.co.absa.subatomic.infrastructure.member.TeamMemberAutomationHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class TeamControllerTest {

    @MockBean
    TeamMemberAutomationHandler teamMemberAutomationHandler;

    @Autowired
    TeamController teamController;

    @Autowired
    TeamMemberController teamMemberController;

    @Autowired
    AtomistConfigurationProperties atomistConfiguration;

    private TeamMemberResource mainMember;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    PlatformTransactionManager platformTransactionManager;

    private TransactionTemplate transactionTemplate;

    private MockRestServiceServer mockServer;

    private Gson gson = new Gson();

    private TeamResource team1;

    private TeamResource team2;

    @Before
    @Transactional
    public void setup_data() throws URISyntaxException {
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
        mockServer = MockRestServiceServer.createServer(restTemplate);

        mockServer.expect(ExpectedCount.twice(),
                requestTo(new URI(this.atomistConfiguration.getTeamCreatedEventUrl())))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED));

        TeamMemberResource newTeamMember = new TeamMemberResource();
        newTeamMember.setFirstName("Steve");
        newTeamMember.setLastName("Carrel");
        newTeamMember.setDomainUsername("office/sc1232");
        newTeamMember.setEmail("steve@office.com");
        newTeamMember.setSlack(new Slack("steve.carrel", "U123335"));

        ResponseEntity<TeamMemberResource> member = this.teamMemberController.onboard(newTeamMember);

        mainMember = Objects.requireNonNull(member.getBody());

        TeamResource team1 = new TeamResource();
        team1.setName("Team 1");
        team1.setDescription("Team 1 Description");
        team1.setCreatedBy(mainMember.getMemberId());
        team1.setOpenShiftCloud("12323");
        team1.setSlack(new za.co.absa.subatomic.adapter.team.rest.Slack("team1"));

        ResponseEntity<TeamResource> teamResourceResponseEntity1 = teamController.create(team1);
        this.team1 = Objects.requireNonNull(teamResourceResponseEntity1.getBody());

        TeamResource team2 = new TeamResource();
        team2.setName("Team 2");
        team2.setDescription("Team 2 Description");
        team2.setCreatedBy(mainMember.getMemberId());
        team2.setOpenShiftCloud("12323");
        team2.setSlack(new za.co.absa.subatomic.adapter.team.rest.Slack("team2"));

        ResponseEntity<TeamResource> teamResourceResponseEntity2 = teamController.create(team2);
        this.team2 = Objects.requireNonNull(teamResourceResponseEntity2.getBody());
    }

    @Test
    @Transactional
    public void when_getTeamsByMemberId_expect_allTeamsAssociatedToMemberReturned() {

        Collection<TeamResource> teamResources = teamController.list("", "", "", "", mainMember.getMemberId()).getContent();

        assertThat(teamResources.size()).isEqualTo(2);

        teamResources.forEach(e -> {
            assertThat(e.getName()).isNotEmpty();
            assertThat(e.getDescription()).isNotEmpty();
            assertThat(e.getCreatedBy()).isNotEmpty();
            assertThat(e.getOpenShiftCloud()).isNotEmpty();
            assertThat(e.getMetadata()).isEmpty();
            assertThat(e.getSlack().getTeamChannel()).isNotEmpty();
        });
    }

    private ResponseEntity<TeamResource> createTeam(TeamResource team) {
        return transactionTemplate.execute(transactionStatus -> this.teamController.create(team));
    }


    @Test
    public void when_addSlackDetailsToTeam_expect_atomistTeamSlackCreatedEventRaised() throws URISyntaxException {
        // ------------ Prepare Mocks and expected results ------------
        mockServer.reset();
        // expect the team created event fired
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(this.atomistConfiguration.getTeamCreatedEventUrl())))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED));

        AtomistMemberBase expectedMember = new AtomistMemberBase("Steve", "office/sc1232", new TeamMemberSlackIdentity("steve.carrel", "U123335"));
        AtomistTeam expectedTeam = new AtomistTeam.Builder()
                .name("Team For Slack Test")
                .openShiftCloud("12323")
                .slackIdentity(
                        new TeamSlackIdentity("team4Slack")
                )
                .owners(Collections.singletonList(expectedMember))
                .build();

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("team", gson.toJsonTree(expectedTeam));
        jsonObject.add("createdBy", gson.toJsonTree(expectedMember));

        // expect the teamSlackChannel created event fired and assert the correct json body is sent
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(this.atomistConfiguration.getTeamSetupCompletedEventUrl())))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(gson.toJson(jsonObject)))
                .andRespond(withStatus(HttpStatus.ACCEPTED));

        // ------------ Perform actions ------------
        // create the team
        TeamResource team = new TeamResource();
        team.setName("Team For Slack Test");
        team.setDescription("Team Description");
        team.setCreatedBy(mainMember.getMemberId());
        team.setOpenShiftCloud("12323");

        ResponseEntity<TeamResource> teamResourceResponseEntity = this.createTeam(team);

        // add the slack channel
        TeamResource teamUpdate = new TeamResource();
        teamUpdate.setCreatedBy(mainMember.getMemberId());
        teamUpdate.setSlack(new za.co.absa.subatomic.adapter.team.rest.Slack("team4Slack"));

        transactionTemplate.execute(transactionStatus -> this.teamController.update(Objects.requireNonNull(teamResourceResponseEntity.getBody()).getTeamId(), teamUpdate));

        // ------------ Verify correct process was called ------------
        mockServer.verify();
    }

    @Test
    public void when_putMetadata_expect_TeamMetaDataToBeOverridden() {
        // expect something to happen
        TeamResource teamResource = new TeamResource();

        MetadataResource metaData = new MetadataResource();
        metaData.setDescription("Test");

        MetadataEntryResource metadataEntryResource = new MetadataEntryResource("a", "1");

        metaData.getMetadataEntries().add(metadataEntryResource);
        teamResource.getMetadata().add(metaData);

        // ------------ Perform actions ------------
        transactionTemplate.execute(transactionStatus -> teamController.update(team1.getTeamId(), teamResource));

        TeamResource body = Objects.requireNonNull(transactionTemplate.execute(transactionStatus -> teamController.get(team1.getTeamId())));

        // ------------ Verify correct process was called ------------
        assertThat(body.getMetadata().size()).isEqualTo(1);
        assertThat(body.getMetadata().get(0).getDescription()).isEqualTo("Test");
        assertThat(body.getMetadata().get(0).getMetadataEntries().get(0).getKey()).isEqualTo("a");
        assertThat(body.getMetadata().get(0).getMetadataEntries().get(0).getValue()).isEqualTo("1");
    }

    @Test
    public void when_team1MetadataIsExtended_expect_team1MetadataWillBeExtended() {
        // expect something to happen
        TeamResource teamResource = new TeamResource();

        MetadataResource metaData = new MetadataResource();
        metaData.setDescription("Test");

        MetadataEntryResource metadataEntryResource = new MetadataEntryResource("a", "1");

        metaData.getMetadataEntries().add(metadataEntryResource);
        teamResource.getMetadata().add(metaData);

        // ------------ Perform actions ------------
        transactionTemplate.execute(transactionStatus -> teamController.patch(team1.getTeamId(), teamResource));

        TeamResource bodyBefore = Objects.requireNonNull(transactionTemplate.execute(transactionStatus -> teamController.get(team1.getTeamId())));

        // ------------ Verify correct process was called ------------
        assertThat(bodyBefore.getMetadata().size()).isEqualTo(1);
        assertThat(bodyBefore.getMetadata().get(0).getDescription()).isEqualTo("Test");
        assertThat(bodyBefore.getMetadata().get(0).getMetadataEntries().get(0).getKey()).isEqualTo("a");
        assertThat(bodyBefore.getMetadata().get(0).getMetadataEntries().get(0).getValue()).isEqualTo("1");

        // ------------ Perform actions ------------
        metadataEntryResource.setValue("2");

        metaData.getMetadataEntries().add(metadataEntryResource);
        teamResource.getMetadata().add(metaData);

        transactionTemplate.execute(transactionStatus -> teamController.patch(team1.getTeamId(), teamResource));

        TeamResource bodyAfter = Objects.requireNonNull(transactionTemplate.execute(transactionStatus -> teamController.get(team1.getTeamId())));

        // ------------ Verify correct process was called ------------
        assertThat(bodyAfter.getMetadata().size()).isEqualTo(1);
        assertThat(bodyAfter.getMetadata().get(0).getDescription()).isEqualTo("Test");
        assertThat(bodyAfter.getMetadata().get(0).getMetadataEntries().get(0).getKey()).isEqualTo("a");
        assertThat(bodyAfter.getMetadata().get(0).getMetadataEntries().get(0).getValue()).isEqualTo("2");
    }
}
