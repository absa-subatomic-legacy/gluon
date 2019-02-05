package za.co.absa.subatomic.adapter.team.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Test;

import za.co.absa.subatomic.infrastructure.member.view.jpa.SlackDetailsEmbedded;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

public class TeamResourceBaseAssemblerTest {
    @Test
    public void toResourceWithFullEntityShouldReturnResource()
            throws Exception {
        TeamMemberEntity member = TeamMemberEntity.builder().memberId("1234")
                .firstName("Kieran").lastName("Bristow")
                .domainUsername("username").email("kieran@mail.com")
                .slackDetails(new SlackDetailsEmbedded("kb", "kb2")).build();
        TeamEntity entity = TeamEntity.builder().id(1L).description("A Team")
                .name("Team").teamId("1235").createdBy(member)
                .openShiftCloud("cloud-name")
                .slackDetails(
                        new za.co.absa.subatomic.infrastructure.team.view.jpa.SlackDetailsEmbedded(
                                "channel"))
                .owners(Collections.singleton(member)).build();

        TeamResourceBaseAssembler assembler = new TeamResourceBaseAssembler();

        TeamResourceBase teamResourceBase = assembler.toResource(entity);

        assertThat(teamResourceBase.getName()).isEqualTo("Team");
        assertThat(teamResourceBase.getDescription()).isEqualTo("A Team");
        assertThat(teamResourceBase.getTeamId()).isEqualTo("1235");
        assertThat(teamResourceBase.getCreatedBy()).isEqualTo("1234");
        assertThat(teamResourceBase.getOpenShiftCloud())
                .isEqualTo("cloud-name");
        assertThat(teamResourceBase.getSlack().getTeamChannel())
                .isEqualTo("channel");
    }

    @Test
    public void toResourceWithEntityWithNoSlackShouldReturnResource()
            throws Exception {
        TeamMemberEntity member = TeamMemberEntity.builder().memberId("1234")
                .firstName("Kieran").lastName("Bristow")
                .domainUsername("username").email("kieran@mail.com")
                .slackDetails(new SlackDetailsEmbedded("kb", "kb2")).build();
        TeamEntity entity = TeamEntity.builder().id(1L).description("A Team")
                .name("Team").teamId("1235").createdBy(member)
                .openShiftCloud("cloud-name")
                .owners(Collections.singleton(member)).build();

        TeamResourceBaseAssembler assembler = new TeamResourceBaseAssembler();

        TeamResourceBase teamResourceBase = assembler.toResource(entity);

        assertThat(teamResourceBase.getName()).isEqualTo("Team");
        assertThat(teamResourceBase.getDescription()).isEqualTo("A Team");
        assertThat(teamResourceBase.getTeamId()).isEqualTo("1235");
        assertThat(teamResourceBase.getCreatedBy()).isEqualTo("1234");
        assertThat(teamResourceBase.getOpenShiftCloud())
                .isEqualTo("cloud-name");
        assertThat(teamResourceBase.getSlack())
                .isEqualTo(null);
    }
}