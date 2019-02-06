package za.co.absa.subatomic.infrastructure.project.view.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import za.co.absa.subatomic.infrastructure.tenant.view.jpa.TenantEntity;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ProjectPersistenceHandlerTest {

    @MockBean
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectPersistenceHandler projectPersistenceHandler;

    @Test
    public void whenUpdateDevDeploymentPipelineWithNewEnvironments_thenNewEnvironmentsAreAddedToPipeline() {

        // Set up initial Project Entity
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
                .description("a project").name("Project 1")
                .owningTenant(tenantEntity)
                .devDeploymentPipeline(deploymentPipelineEntity)
                .releaseDeploymentPipelines(new ArrayList<>())
                .build();

        // Mock find response
        Mockito.when(projectRepository.findByProjectId("1"))
                .thenReturn(projectEntity);

        // Setup new dev deployment pipeline
        DevDeploymentPipelineEntity updatedDeploymentPipelineEntity = DevDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .build();
        DevDeploymentEnvironmentEntity updatedDevDeploymentEnvironmentEntity = DevDeploymentEnvironmentEntity
                .builder()
                .displayName("Dev")
                .pipeline(updatedDeploymentPipelineEntity)
                .postfix("dev").build();
        DevDeploymentEnvironmentEntity updatedUatDeploymentEnvironmentEntity = DevDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT")
                .pipeline(updatedDeploymentPipelineEntity)
                .postfix("uat").build();
        updatedDeploymentPipelineEntity.getEnvironments()
                .add(updatedDevDeploymentEnvironmentEntity);
        updatedDeploymentPipelineEntity.getEnvironments()
                .add(updatedUatDeploymentEnvironmentEntity);

        ProjectEntity updatedProject = this.projectPersistenceHandler
                .updateDevDeploymentPipeline("1",
                        updatedDeploymentPipelineEntity);

        assertThat(updatedProject.getDevDeploymentPipeline().getEnvironments()
                .size()).isEqualTo(2);
        assertThat(updatedProject.getDevDeploymentPipeline().getEnvironments()
                .get(1).getDisplayName()).isEqualTo("UAT");
        assertThat(updatedProject.getDevDeploymentPipeline().getEnvironments()
                .get(1).getPositionInPipeline()).isEqualTo(2);
    }

    @Test
    public void whenUpdateDevDeploymentPipelineWithUpdatedEnvironments_thenOldEnvironmentsAreUpdated() {

        // Set up initial Project Entity
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
                .description("a project").name("Project 1")
                .owningTenant(tenantEntity)
                .devDeploymentPipeline(deploymentPipelineEntity)
                .releaseDeploymentPipelines(new ArrayList<>())
                .build();

        // Mock find response
        Mockito.when(projectRepository.findByProjectId("1"))
                .thenReturn(projectEntity);

        // Setup new dev deployment pipeline
        DevDeploymentPipelineEntity updatedDeploymentPipelineEntity = DevDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .build();
        DevDeploymentEnvironmentEntity updatedDevDeploymentEnvironmentEntity = DevDeploymentEnvironmentEntity
                .builder()
                .displayName("Dev New")
                .pipeline(updatedDeploymentPipelineEntity)
                .postfix("dev").build();
        updatedDeploymentPipelineEntity.getEnvironments()
                .add(updatedDevDeploymentEnvironmentEntity);

        ProjectEntity updatedProject = this.projectPersistenceHandler
                .updateDevDeploymentPipeline("1",
                        updatedDeploymentPipelineEntity);

        assertThat(updatedProject.getDevDeploymentPipeline().getEnvironments()
                .size()).isEqualTo(1);
        assertThat(updatedProject.getDevDeploymentPipeline().getEnvironments()
                .get(0).getDisplayName()).isEqualTo("Dev New");
        assertThat(updatedProject.getDevDeploymentPipeline().getEnvironments()
                .get(0).getPositionInPipeline()).isEqualTo(1);
    }

    @Test
    public void whenUpdateDevDeploymentPipelineWithRemovedEnvironments_thenOldExcludedEnvironmentsAreRemoved() {

        // Set up initial Project Entity
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
                .positionInPipeline(1)
                .postfix("uat").build();

        deploymentPipelineEntity.getEnvironments()
                .add(devDeploymentEnvironmentEntity);
        deploymentPipelineEntity.getEnvironments()
                .add(uatDeploymentEnvironmentEntity);
        TenantEntity tenantEntity = TenantEntity.builder().tenantId("2")
                .name("tenant").description("tenant desc").build();
        ProjectEntity projectEntity = ProjectEntity.builder().projectId("1")
                .applications(new HashSet<>())
                .description("a project").name("Project 1")
                .owningTenant(tenantEntity)
                .devDeploymentPipeline(deploymentPipelineEntity)
                .releaseDeploymentPipelines(new ArrayList<>())
                .build();

        // Mock find response
        Mockito.when(projectRepository.findByProjectId("1"))
                .thenReturn(projectEntity);

        // Setup new dev deployment pipeline
        DevDeploymentPipelineEntity updatedDeploymentPipelineEntity = DevDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .build();
        DevDeploymentEnvironmentEntity updatedUatDeploymentEnvironmentEntity = DevDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT")
                .pipeline(updatedDeploymentPipelineEntity)
                .postfix("uat").build();
        updatedDeploymentPipelineEntity.getEnvironments()
                .add(updatedUatDeploymentEnvironmentEntity);

        ProjectEntity updatedProject = this.projectPersistenceHandler
                .updateDevDeploymentPipeline("1",
                        updatedDeploymentPipelineEntity);

        assertThat(updatedProject.getDevDeploymentPipeline().getEnvironments()
                .size()).isEqualTo(1);
        assertThat(updatedProject.getDevDeploymentPipeline().getEnvironments()
                .get(0).getDisplayName()).isEqualTo("UAT");
        assertThat(updatedProject.getDevDeploymentPipeline().getEnvironments()
                .get(0).getPositionInPipeline()).isEqualTo(1);
    }

    @Test
    public void whenUpdateReleaseDeploymentPipelinesWithNewEnvironments_thenPipelineIsUpdatedWithNewEnvironments() {

        // Set up initial Project Entity
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
                .description("a project").name("Project 1")
                .owningTenant(tenantEntity)
                .releaseDeploymentPipelines(new ArrayList<>())
                .build();
        projectEntity.getReleaseDeploymentPipelines()
                .add(releaseDeploymentPipelineEntity);

        // Mock find response
        Mockito.when(projectRepository.findByProjectId("1"))
                .thenReturn(projectEntity);

        // Setup new release deployment pipeline
        ReleaseDeploymentPipelineEntity updatedReleaseDeploymentPipelineEntity = ReleaseDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .tag("tag")
                .build();

        ReleaseDeploymentEnvironmentEntity updatedUatReleaseDeploymentEnvironmentEntity = ReleaseDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT")
                .pipeline(releaseDeploymentPipelineEntity)
                .postfix("uat").build();
        ReleaseDeploymentEnvironmentEntity updatedPreProdReleaseDeploymentEnvironmentEntity = ReleaseDeploymentEnvironmentEntity
                .builder()
                .displayName("Pre Prod")
                .pipeline(releaseDeploymentPipelineEntity)
                .postfix("pre-prod").build();
        updatedReleaseDeploymentPipelineEntity.getEnvironments()
                .add(updatedUatReleaseDeploymentEnvironmentEntity);
        updatedReleaseDeploymentPipelineEntity.getEnvironments()
                .add(updatedPreProdReleaseDeploymentEnvironmentEntity);

        ProjectEntity updatedProject = this.projectPersistenceHandler
                .updateReleaseDeploymentPipelines("1",
                        Collections.singletonList(
                                updatedReleaseDeploymentPipelineEntity));

        assertThat(updatedProject.getReleaseDeploymentPipelines()
                .size()).isEqualTo(1);
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .size()).isEqualTo(2);
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .get(0).getDisplayName()).isEqualTo("UAT");
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .get(0).getPositionInPipeline()).isEqualTo(1);
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .get(1).getDisplayName()).isEqualTo("Pre Prod");
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .get(1).getPositionInPipeline()).isEqualTo(2);
    }

    @Test
    public void whenUpdateReleaseDeploymentPipelinesWithUpdatedEnvironments_thenOldEnvironmentsAreUpdated() {

        // Set up initial Project Entity
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
                .description("a project").name("Project 1")
                .owningTenant(tenantEntity)
                .releaseDeploymentPipelines(new ArrayList<>())
                .build();
        projectEntity.getReleaseDeploymentPipelines()
                .add(releaseDeploymentPipelineEntity);

        // Mock find response
        Mockito.when(projectRepository.findByProjectId("1"))
                .thenReturn(projectEntity);

        // Setup new release deployment pipeline
        ReleaseDeploymentPipelineEntity updatedReleaseDeploymentPipelineEntity = ReleaseDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .tag("tag")
                .build();

        ReleaseDeploymentEnvironmentEntity updatedReleaseDeploymentEnvironmentEntity = ReleaseDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT New")
                .pipeline(releaseDeploymentPipelineEntity)
                .postfix("uat").build();
        updatedReleaseDeploymentPipelineEntity.getEnvironments()
                .add(updatedReleaseDeploymentEnvironmentEntity);

        ProjectEntity updatedProject = this.projectPersistenceHandler
                .updateReleaseDeploymentPipelines("1",
                        Collections.singletonList(
                                updatedReleaseDeploymentPipelineEntity));

        assertThat(updatedProject.getReleaseDeploymentPipelines()
                .size()).isEqualTo(1);
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .size()).isEqualTo(1);
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .get(0).getDisplayName()).isEqualTo("UAT New");
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .get(0).getPositionInPipeline()).isEqualTo(1);
    }

    @Test
    public void whenUpdateReleaseDeploymentPipelinesWithRemovedEnvironments_thenOldExcludedEnvironments() {

        // Set up initial Project Entity
        ReleaseDeploymentPipelineEntity releaseDeploymentPipelineEntity = ReleaseDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .tag("tag")
                .build();

        ReleaseDeploymentEnvironmentEntity uatReleaseDeploymentEnvironmentEntity = ReleaseDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT")
                .pipeline(releaseDeploymentPipelineEntity)
                .positionInPipeline(1)
                .postfix("uat").build();
        ReleaseDeploymentEnvironmentEntity preprodReleaseDeploymentEnvironmentEntity = ReleaseDeploymentEnvironmentEntity
                .builder()
                .displayName("Pre Prod")
                .pipeline(releaseDeploymentPipelineEntity)
                .positionInPipeline(1)
                .postfix("pre-prod").build();

        releaseDeploymentPipelineEntity.getEnvironments()
                .add(uatReleaseDeploymentEnvironmentEntity);
        releaseDeploymentPipelineEntity.getEnvironments()
                .add(preprodReleaseDeploymentEnvironmentEntity);

        TenantEntity tenantEntity = TenantEntity.builder().tenantId("2")
                .name("tenant").description("tenant desc").build();
        ProjectEntity projectEntity = ProjectEntity.builder().projectId("1")
                .applications(new HashSet<>())
                .description("a project").name("Project 1")
                .owningTenant(tenantEntity)
                .releaseDeploymentPipelines(new ArrayList<>())
                .build();
        projectEntity.getReleaseDeploymentPipelines()
                .add(releaseDeploymentPipelineEntity);

        // Mock find response
        Mockito.when(projectRepository.findByProjectId("1"))
                .thenReturn(projectEntity);

        // Setup new release deployment pipeline
        ReleaseDeploymentPipelineEntity updatedReleaseDeploymentPipelineEntity = ReleaseDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .tag("tag")
                .build();

        ReleaseDeploymentEnvironmentEntity updatedReleaseDeploymentEnvironmentEntity = ReleaseDeploymentEnvironmentEntity
                .builder()
                .displayName("Pre Prod")
                .pipeline(releaseDeploymentPipelineEntity)
                .postfix("pre-prod").build();
        updatedReleaseDeploymentPipelineEntity.getEnvironments()
                .add(updatedReleaseDeploymentEnvironmentEntity);

        ProjectEntity updatedProject = this.projectPersistenceHandler
                .updateReleaseDeploymentPipelines("1",
                        Collections.singletonList(
                                updatedReleaseDeploymentPipelineEntity));

        assertThat(updatedProject.getReleaseDeploymentPipelines()
                .size()).isEqualTo(1);
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .size()).isEqualTo(1);
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .get(0).getDisplayName()).isEqualTo("Pre Prod");
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .get(0).getPositionInPipeline()).isEqualTo(1);
    }

    @Test
    public void whenUpdateReleaseDeploymentPipelinesWithNewPipeline_thenAddNewPipeline() {

        // Set up initial Project Entity
        ReleaseDeploymentPipelineEntity releaseDeploymentPipelineEntity = ReleaseDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .tag("tag")
                .build();

        ReleaseDeploymentEnvironmentEntity uatReleaseDeploymentEnvironmentEntity = ReleaseDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT1")
                .pipeline(releaseDeploymentPipelineEntity)
                .positionInPipeline(1)
                .postfix("uat1").build();

        releaseDeploymentPipelineEntity.getEnvironments()
                .add(uatReleaseDeploymentEnvironmentEntity);

        TenantEntity tenantEntity = TenantEntity.builder().tenantId("2")
                .name("tenant").description("tenant desc").build();
        ProjectEntity projectEntity = ProjectEntity.builder().projectId("1")
                .applications(new HashSet<>())
                .description("a project").name("Project 1")
                .owningTenant(tenantEntity)
                .releaseDeploymentPipelines(new ArrayList<>())
                .build();
        projectEntity.getReleaseDeploymentPipelines()
                .add(releaseDeploymentPipelineEntity);

        // Mock find response
        Mockito.when(projectRepository.findByProjectId("1"))
                .thenReturn(projectEntity);

        // Setup new release deployment pipeline
        ReleaseDeploymentPipelineEntity updatedReleaseDeploymentPipelineEntity = ReleaseDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .tag("tag")
                .build();

        ReleaseDeploymentEnvironmentEntity updatedUatReleaseDeploymentEnvironmentEntity = ReleaseDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT1")
                .pipeline(updatedReleaseDeploymentPipelineEntity)
                .positionInPipeline(1)
                .postfix("uat1").build();

        updatedReleaseDeploymentPipelineEntity.getEnvironments()
                .add(updatedUatReleaseDeploymentEnvironmentEntity);

        ReleaseDeploymentPipelineEntity updatedReleaseDeploymentPipelineEntity2 = ReleaseDeploymentPipelineEntity
                .builder()
                .pipelineId("2")
                .environments(new ArrayList<>())
                .name("some pipeline 2")
                .tag("tag 2")
                .build();

        ReleaseDeploymentEnvironmentEntity updatedUatReleaseDeploymentEnvironmentEntity2 = ReleaseDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT2")
                .pipeline(updatedReleaseDeploymentPipelineEntity2)
                .positionInPipeline(1)
                .postfix("uat2").build();

        updatedReleaseDeploymentPipelineEntity2.getEnvironments()
                .add(updatedUatReleaseDeploymentEnvironmentEntity2);

        ProjectEntity updatedProject = this.projectPersistenceHandler
                .updateReleaseDeploymentPipelines("1",
                        Arrays.asList(
                                updatedReleaseDeploymentPipelineEntity,
                                updatedReleaseDeploymentPipelineEntity2));

        assertThat(updatedProject.getReleaseDeploymentPipelines()
                .size()).isEqualTo(2);
        assertThat(
                updatedProject.getReleaseDeploymentPipelines().get(0).getName())
                        .isEqualTo("some pipeline");
        assertThat(
                updatedProject.getReleaseDeploymentPipelines().get(1).getName())
                        .isEqualTo("some pipeline 2");
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .size()).isEqualTo(1);
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .get(0).getDisplayName()).isEqualTo("UAT1");
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .get(0).getPositionInPipeline()).isEqualTo(1);
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(1)
                .getEnvironments()
                .get(0).getDisplayName()).isEqualTo("UAT2");
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(1)
                .getEnvironments()
                .get(0).getPositionInPipeline()).isEqualTo(1);
    }

    @Test
    public void whenUpdateReleaseDeploymentPipelinesWithRemovedPipeline_thenRemoveExcludedPipeline() {

        // Set up initial Project Entity
        ReleaseDeploymentPipelineEntity releaseDeploymentPipelineEntity = ReleaseDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .tag("tag")
                .build();

        ReleaseDeploymentEnvironmentEntity uatReleaseDeploymentEnvironmentEntity = ReleaseDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT1")
                .pipeline(releaseDeploymentPipelineEntity)
                .positionInPipeline(1)
                .postfix("uat1").build();

        releaseDeploymentPipelineEntity.getEnvironments()
                .add(uatReleaseDeploymentEnvironmentEntity);

        ReleaseDeploymentPipelineEntity releaseDeploymentPipelineEntity2 = ReleaseDeploymentPipelineEntity
                .builder()
                .pipelineId("2")
                .environments(new ArrayList<>())
                .name("some pipeline 2")
                .tag("tag 2")
                .build();

        ReleaseDeploymentEnvironmentEntity uatReleaseDeploymentEnvironmentEntity2 = ReleaseDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT2")
                .pipeline(releaseDeploymentPipelineEntity2)
                .positionInPipeline(1)
                .postfix("uat2").build();

        releaseDeploymentPipelineEntity2.getEnvironments()
                .add(uatReleaseDeploymentEnvironmentEntity2);

        TenantEntity tenantEntity = TenantEntity.builder().tenantId("2")
                .name("tenant").description("tenant desc").build();
        ProjectEntity projectEntity = ProjectEntity.builder().projectId("1")
                .applications(new HashSet<>())
                .description("a project").name("Project 1")
                .owningTenant(tenantEntity)
                .releaseDeploymentPipelines(new ArrayList<>())
                .build();
        projectEntity.getReleaseDeploymentPipelines()
                .add(releaseDeploymentPipelineEntity);
        projectEntity.getReleaseDeploymentPipelines()
                .add(releaseDeploymentPipelineEntity2);

        // Mock find response
        Mockito.when(projectRepository.findByProjectId("1"))
                .thenReturn(projectEntity);

        // Setup new release deployment pipeline
        ReleaseDeploymentPipelineEntity updatedReleaseDeploymentPipelineEntity = ReleaseDeploymentPipelineEntity
                .builder()
                .pipelineId("1")
                .environments(new ArrayList<>())
                .name("some pipeline")
                .tag("tag")
                .build();

        ReleaseDeploymentEnvironmentEntity updatedUatReleaseDeploymentEnvironmentEntity = ReleaseDeploymentEnvironmentEntity
                .builder()
                .displayName("UAT1")
                .pipeline(updatedReleaseDeploymentPipelineEntity)
                .positionInPipeline(1)
                .postfix("uat1").build();

        updatedReleaseDeploymentPipelineEntity.getEnvironments()
                .add(updatedUatReleaseDeploymentEnvironmentEntity);

        ProjectEntity updatedProject = this.projectPersistenceHandler
                .updateReleaseDeploymentPipelines("1",
                        Collections.singletonList(
                                updatedReleaseDeploymentPipelineEntity));

        assertThat(updatedProject.getReleaseDeploymentPipelines()
                .size()).isEqualTo(1);
        assertThat(
                updatedProject.getReleaseDeploymentPipelines().get(0).getName())
                        .isEqualTo("some pipeline");
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .size()).isEqualTo(1);
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .get(0).getDisplayName()).isEqualTo("UAT1");
        assertThat(updatedProject.getReleaseDeploymentPipelines().get(0)
                .getEnvironments()
                .get(0).getPositionInPipeline()).isEqualTo(1);

    }
}