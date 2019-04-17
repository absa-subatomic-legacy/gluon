package za.co.absa.subatomic.adapter.team.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import za.co.absa.subatomic.adapter.member.rest.Slack;
import za.co.absa.subatomic.adapter.member.rest.TeamMemberController;
import za.co.absa.subatomic.adapter.member.rest.TeamMemberResource;
import za.co.absa.subatomic.infrastructure.member.TeamMemberAutomationHandler;
import za.co.absa.subatomic.infrastructure.team.TeamAutomationHandler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Objects;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class TeamControllerTest {

    @MockBean
    TeamMemberAutomationHandler teamMemberAutomationHandler;

    @MockBean
    TeamAutomationHandler teamAutomationHandler;

    @Autowired
    TeamController teamController;

    @Autowired
    TeamMemberController teamMemberController;

    TeamMemberResource mainMember;

    @Before
    @Transactional
    public void setup_data() {

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

        teamController.create(team1);

        TeamResource team2 = new TeamResource();
        team2.setName("Team 2");
        team2.setDescription("Team 2 Description");
        team2.setCreatedBy(mainMember.getMemberId());
        team2.setOpenShiftCloud("12323");
        team2.setSlack(new za.co.absa.subatomic.adapter.team.rest.Slack("team2"));

        teamController.create(team2);
    }

    @Test
    @Transactional
    public void when_getTeamsByMemberId_expect_allTeamsAssociatedToMemberReturned() {

        Collection<TeamResource> teamResources = teamController.list("", "", "", "", mainMember.getMemberId()).getContent();

        assertThat(teamResources.size()).isEqualTo(2);

        teamResources.stream().forEach(e -> {
            assertThat(e.getName()).isNotEmpty();
            assertThat(e.getDescription()).isNotEmpty();
            assertThat(e.getCreatedBy()).isNotEmpty();
            assertThat(e.getOpenShiftCloud()).isNotEmpty();
            assertThat(e.getSlack().getTeamChannel()).isNotEmpty();
        });
    }

}