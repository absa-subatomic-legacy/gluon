package za.co.absa.subatomic.infrastructure.atomist.resource.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import za.co.absa.subatomic.infrastructure.member.view.jpa.SlackDetailsEmbedded;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.DevDeploymentEnvironmentEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.DevDeploymentPipelineEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ReleaseDeploymentEnvironmentEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ReleaseDeploymentPipelineEntity;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;
import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;

public class AtomistProjectMapperTest {

    @Test
    public void whenCreateAtomistDeploymentEnvironmentWithValidDeploymentEnvironment_thenReturnCorrectTransformation()
            throws Exception {
        DevDeploymentEnvironmentEntity deploymentEnvironmentEntity = DevDeploymentEnvironmentEntity
                .builder()
                .displayName("Env")
                .positionInPipeline(1)
                .postfix("uat").build();

        AtomistProjectMapper projectMapper = new AtomistProjectMapper();

        AtomistDeploymentEnvironment atomistDeploymentEnvironment = projectMapper
                .createAtomistDeploymentEnvironment(
                        deploymentEnvironmentEntity);

        assertThat(atomistDeploymentEnvironment.getDisplayName())
                .isEqualTo("Env");
        assertThat(atomistDeploymentEnvironment.getPostfix())
                .isEqualTo("uat");
        assertThat(atomistDeploymentEnvironment.getPositionInPipeline())
                .isEqualTo(1);
    }

    @Test
    public void whenCreateAtomistDeploymentPipelineWithValidDeploymentPipelineAndDefinedEnvironments_thenReturnCorrectTransformation()
            throws Exception {
        DevDeploymentPipelineEntity deploymentPipelineEntity = DevDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .build();
        DevDeploymentEnvironmentEntity devDeploymentEnvironmentEntity = DevDeploymentEnvironmentEntity
                .builder()
                .displayName("Dev")
                .pipeline(deploymentPipelineEntity)
                .positionInPipeline(1)
                .postfix("dev").build();
        DevDeploymentEnvironmentEntity uatDeploymentEnvironmentEntity = DevDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT")
                .pipeline(deploymentPipelineEntity)
                .positionInPipeline(2)
                .postfix("uat").build();
        deploymentPipelineEntity.getEnvironments()
                .add(devDeploymentEnvironmentEntity);
        deploymentPipelineEntity.getEnvironments()
                .add(uatDeploymentEnvironmentEntity);

        AtomistProjectMapper projectMapper = new AtomistProjectMapper();

        AtomistDeploymentPipeline atomistDeploymentPipeline = projectMapper
                .createAtomistDeploymentPipeline(
                        deploymentPipelineEntity);

        assertThat(atomistDeploymentPipeline.getName())
                .isEqualTo("some pipeline");
        assertThat(atomistDeploymentPipeline.getPipelineId()).isEqualTo("1");
        assertThat(atomistDeploymentPipeline.getTag()).isEqualTo("");
        assertThat(
                atomistDeploymentPipeline.getEnvironments().get(0).getPostfix())
                        .isEqualTo("dev");
        assertThat(
                atomistDeploymentPipeline.getEnvironments().get(1).getPostfix())
                        .isEqualTo("uat");

    }

    @Test
    public void whenCreateAtomistDeploymentPipelineWithValidDeploymentPipelineAndNoEnvironments_thenReturnCorrectTransformation()
            throws Exception {
        ReleaseDeploymentPipelineEntity deploymentPipelineEntity = ReleaseDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .tag("tag")
                .build();

        AtomistProjectMapper projectMapper = new AtomistProjectMapper();

        AtomistDeploymentPipeline atomistDeploymentPipeline = projectMapper
                .createAtomistDeploymentPipeline(
                        deploymentPipelineEntity);

        assertThat(atomistDeploymentPipeline.getName())
                .isEqualTo("some pipeline");
        assertThat(atomistDeploymentPipeline.getPipelineId()).isEqualTo("1");
        assertThat(atomistDeploymentPipeline.getTag()).isEqualTo("tag");
        assertThat(
                atomistDeploymentPipeline.getEnvironments().size())
                        .isEqualTo(0);

    }

    @Test
    public void whenCreateAtomistProjectWithProjectEntityAndNoReleasePipelines_thenReturnCorrectTransform() {
        TeamMemberEntity member = TeamMemberEntity.builder().memberId("1234")
                .firstName("Kieran").lastName("Bristow")
                .domainUsername("username").email("kieran@mail.com")
                .slackDetails(new SlackDetailsEmbedded("kb", "kb2")).build();
        TeamEntity team = TeamEntity.builder().description("A Team")
                .name("Team").teamId("1235").createdBy(member)
                .openShiftCloud("cloud-name")
                .owners(Collections.singleton(member)).build();
        DevDeploymentPipelineEntity deploymentPipelineEntity = DevDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .build();
        DevDeploymentEnvironmentEntity devDeploymentEnvironmentEntity = DevDeploymentEnvironmentEntity
                .builder()
                .displayName("Dev")
                .pipeline(deploymentPipelineEntity)
                .positionInPipeline(1)
                .postfix("dev").build();
        DevDeploymentEnvironmentEntity uatDeploymentEnvironmentEntity = DevDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT")
                .pipeline(deploymentPipelineEntity)
                .positionInPipeline(2)
                .postfix("uat").build();

        deploymentPipelineEntity.getEnvironments()
                .add(devDeploymentEnvironmentEntity);
        deploymentPipelineEntity.getEnvironments()
                .add(uatDeploymentEnvironmentEntity);
        TenantEntity tenantEntity = TenantEntity.builder().tenantId("2")
                .name("tenant").description("tenant desc").build();
        ProjectEntity projectEntity = ProjectEntity.builder().projectId("1")
                .applications(new HashSet<>())
                .createdBy(member)
                .description("a project").name("Project 1")
                .owningTeam(team)
                .owningTenant(tenantEntity)
                .devDeploymentPipeline(deploymentPipelineEntity)
                .releaseDeploymentPipelines(new ArrayList<>())
                .build();

        AtomistProjectMapper projectMapper = new AtomistProjectMapper();
        AtomistProject atomistProject = projectMapper
                .createAtomistProject(projectEntity);

        assertThat(atomistProject.getName()).isEqualTo("Project 1");
        assertThat(atomistProject.getDescription()).isEqualTo("a project");
        assertThat(atomistProject.getProjectId()).isEqualTo("1");
        assertThat(atomistProject.getTeam().getTeamId()).isEqualTo("1235");
        assertThat(atomistProject.getDevDeploymentPipeline().getName())
                .isEqualTo("some pipeline");
        assertThat(atomistProject.getReleaseDeploymentPipelines().size())
                .isEqualTo(0);
        assertThat(atomistProject.getTenant().getTenantId()).isEqualTo("2");
        assertThat(atomistProject.getCreatedBy().getTeamMemberId())
                .isEqualTo("1234");
    }

    @Test
    public void whenCreateAtomistProjectWithProjectEntityAndReleasePipelines_thenReturnCorrectTransform() {
        TeamMemberEntity member = TeamMemberEntity.builder().memberId("1234")
                .firstName("Kieran").lastName("Bristow")
                .domainUsername("username").email("kieran@mail.com")
                .slackDetails(new SlackDetailsEmbedded("kb", "kb2")).build();
        TeamEntity team = TeamEntity.builder().description("A Team")
                .name("Team").teamId("1235").createdBy(member)
                .openShiftCloud("cloud-name")
                .owners(Collections.singleton(member)).build();
        DevDeploymentPipelineEntity deploymentPipelineEntity = DevDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("Dev Pipeline")
                .build();
        DevDeploymentEnvironmentEntity devDeploymentEnvironmentEntity = DevDeploymentEnvironmentEntity
                .builder()
                .displayName("Dev")
                .pipeline(deploymentPipelineEntity)
                .positionInPipeline(1)
                .postfix("dev").build();

        deploymentPipelineEntity.getEnvironments()
                .add(devDeploymentEnvironmentEntity);

        ReleaseDeploymentPipelineEntity releaseDeploymentPipelineEntity = ReleaseDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .tag("tag")
                .build();

        ReleaseDeploymentEnvironmentEntity releaseDeploymentEnvironmentEntity = ReleaseDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT")
                .pipeline(releaseDeploymentPipelineEntity)
                .positionInPipeline(1)
                .postfix("uat").build();
        releaseDeploymentPipelineEntity.getEnvironments()
                .add(releaseDeploymentEnvironmentEntity);

        TenantEntity tenantEntity = TenantEntity.builder().tenantId("2")
                .name("tenant").description("tenant desc").build();
        ProjectEntity projectEntity = ProjectEntity.builder().projectId("1")
                .applications(new HashSet<>())
                .createdBy(member)
                .description("a project").name("Project 1")
                .owningTeam(team)
                .owningTenant(tenantEntity)
                .devDeploymentPipeline(deploymentPipelineEntity)
                .releaseDeploymentPipelines(Collections
                        .singletonList(releaseDeploymentPipelineEntity))
                .build();

        AtomistProjectMapper projectMapper = new AtomistProjectMapper();
        AtomistProject atomistProject = projectMapper
                .createAtomistProject(projectEntity);

        assertThat(atomistProject.getName()).isEqualTo("Project 1");
        assertThat(atomistProject.getDescription()).isEqualTo("a project");
        assertThat(atomistProject.getProjectId()).isEqualTo("1");
        assertThat(atomistProject.getTeam().getTeamId()).isEqualTo("1235");
        assertThat(atomistProject.getDevDeploymentPipeline().getName())
                .isEqualTo("Dev Pipeline");
        assertThat(
                atomistProject.getReleaseDeploymentPipelines().get(0).getName())
                        .isEqualTo("some pipeline");
        assertThat(
                atomistProject.getReleaseDeploymentPipelines().get(0)
                        .getEnvironments().get(0).getDisplayName())
                                .isEqualTo("UAT");
        assertThat(atomistProject.getTenant().getTenantId()).isEqualTo("2");
        assertThat(atomistProject.getCreatedBy().getTeamMemberId())
                .isEqualTo("1234");
    }

    @Test
    public void whenCreateAtomistProjectBaseWithSimpleProjectEntity_thenReturnCorrectTransform() {
        TeamMemberEntity member = TeamMemberEntity.builder().memberId("1234")
                .firstName("Kieran").lastName("Bristow")
                .domainUsername("username").email("kieran@mail.com")
                .slackDetails(new SlackDetailsEmbedded("kb", "kb2")).build();
        TeamEntity team = TeamEntity.builder().description("A Team")
                .name("Team").teamId("1235").createdBy(member)
                .openShiftCloud("cloud-name")
                .owners(Collections.singleton(member)).build();
        DevDeploymentPipelineEntity deploymentPipelineEntity = DevDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .build();
        DevDeploymentEnvironmentEntity devDeploymentEnvironmentEntity = DevDeploymentEnvironmentEntity
                .builder()
                .displayName("Dev")
                .pipeline(deploymentPipelineEntity)
                .positionInPipeline(1)
                .postfix("dev").build();

        deploymentPipelineEntity.getEnvironments()
                .add(devDeploymentEnvironmentEntity);
        TenantEntity tenantEntity = TenantEntity.builder().tenantId("2")
                .name("tenant").description("tenant desc").build();
        ProjectEntity projectEntity = ProjectEntity.builder().projectId("1")
                .applications(new HashSet<>())
                .createdBy(member)
                .description("a project").name("Project 1")
                .owningTeam(team)
                .owningTenant(tenantEntity)
                .devDeploymentPipeline(deploymentPipelineEntity)
                .releaseDeploymentPipelines(new ArrayList<>())
                .build();

        AtomistProjectMapper projectMapper = new AtomistProjectMapper();
        AtomistProjectBase atomistProject = projectMapper
                .createAtomistProjectBase(projectEntity);

        assertThat(atomistProject.getName()).isEqualTo("Project 1");
        assertThat(atomistProject.getDescription()).isEqualTo("a project");
        assertThat(atomistProject.getProjectId()).isEqualTo("1");
        assertThat(atomistProject.getTeam().getTeamId()).isEqualTo("1235");
        assertThat(atomistProject.getTenant().getTenantId()).isEqualTo("2");
        assertThat(atomistProject.getCreatedBy().getTeamMemberId())
                .isEqualTo("1234");
    }
}