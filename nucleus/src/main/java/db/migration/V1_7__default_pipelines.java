package db.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V1_7__default_pipelines extends BaseJavaMigration {
    static int devPipelineIdCounter = 1;

    static int devEnvironmentIdCounter = 1;

    static int releasePipelineIdCounter = 1;

    static int releaseEnvironmentIdCounter = 1;

    public void migrate(Context context) throws Exception {
        try (Statement select = context.getConnection().createStatement()) {
            try (ResultSet rows = select
                    .executeQuery("SELECT ID, NAME FROM project")) {
                while (rows.next()) {
                    int projectId = rows.getInt(1);
                    String projectName = rows.getString(2);
                    createDevPipelinesForProject(context, projectId);
                    createReleasePipelinesForProject(context, projectId,
                            projectName);
                }
            }
        }
    }

    private void createDevPipelinesForProject(Context context, int projectId)
            throws SQLException {
        try (PreparedStatement create_new_dev_pipeline = context
                .getConnection()
                .prepareStatement(
                        "INSERT INTO dev_deployment_pipeline(ID, PIPELINE_ID, NAME) VALUES("
                                + devPipelineIdCounter
                                + ",'" + UUID.randomUUID().toString() + "'"
                                + ",'Default')")) {
            create_new_dev_pipeline.executeUpdate();
            int pipelineId = devPipelineIdCounter++;

            try (PreparedStatement associate_pipeline_to_project = context
                    .getConnection().prepareStatement(
                            "UPDATE project SET dev_deployment_pipeline_id = "
                                    + pipelineId + " where ID = "
                                    + projectId)) {
                associate_pipeline_to_project.executeUpdate();
                this.createDefaultEnvironmentsForDevPipeline(context,
                        pipelineId);
            }
        }
    }

    private void createDefaultEnvironmentsForDevPipeline(Context context,
            int pipelineId) throws SQLException {
        String[] environments = new String[] { "dev", "sit" };
        for (int i = 0; i < environments.length; i++) {
            String environment = environments[i];
            try (PreparedStatement create_new_dev_environment = context
                    .getConnection()
                    .prepareStatement(
                            "INSERT INTO dev_deployment_environment(ID, DISPLAY_NAME, POSITION_IN_PIPELINE, POSTFIX, PIPELINE_ID) VALUES("
                                    + devEnvironmentIdCounter + ",'"
                                    + environment.toUpperCase() + "'," + i
                                    + ",'"
                                    + environment + "'," + pipelineId + ")")) {
                create_new_dev_environment.executeUpdate();
                int environmentId = devEnvironmentIdCounter++;
                try (PreparedStatement associate_pipeline_to_project = context
                        .getConnection().prepareStatement(
                                "INSERT INTO dev_deployment_pipeline_environments(DEV_DEPLOYMENT_PIPELINE_ENTITY_ID, ENVIRONMENTS_ID) VALUES("
                                        + pipelineId + ","
                                        + environmentId + ")")) {
                    associate_pipeline_to_project.executeUpdate();
                }
            }
        }
    }

    private void createReleasePipelinesForProject(Context context,
            int projectId,
            String projectName)
            throws SQLException {
        try (PreparedStatement create_new_release_pipeline = context
                .getConnection()
                .prepareStatement(
                        "INSERT INTO release_deployment_pipeline(ID, PIPELINE_ID, NAME, TAG) VALUES("
                                + releasePipelineIdCounter
                                + ",'" + UUID.randomUUID().toString() + "'"
                                + ",'Default', '')")) {
            create_new_release_pipeline.executeUpdate();
            int pipelineId = releasePipelineIdCounter++;

            // Associate pipeline to project and create default environments
            try (PreparedStatement associate_pipeline_to_project = context
                    .getConnection().prepareStatement(
                            "INSERT INTO project_release_deployment_pipelines(PROJECT_ENTITY_ID, RELEASE_DEPLOYMENT_PIPELINES_ID) VALUES ("
                                    + projectId + ","
                                    + pipelineId + ")")) {
                associate_pipeline_to_project.executeUpdate();
                this.createDefaultEnvironmentsForReleasePipeline(context,
                        pipelineId);
            }

            // Update Project Prod Requests
            try (PreparedStatement update_project_prod_requests = context
                    .getConnection().prepareStatement(
                            "UPDATE project_prod_request SET DEPLOYMENT_PIPELINE_ID = "
                                    + pipelineId
                                    + " WHERE PROJECT_ID = "
                                    + projectId)) {
                update_project_prod_requests.executeUpdate();
            }

            // Update Generic Prod Requests
            try (PreparedStatement update_generic_prod_requests = context
                    .getConnection().prepareStatement(
                            "UPDATE generic_prod_request SET DEPLOYMENT_PIPELINE_ID = "
                                    + pipelineId
                                    + " WHERE PROJECT_ID = "
                                    + projectId)) {
                update_generic_prod_requests.executeUpdate();
            }

            // Update Application Prod Requests
            try (PreparedStatement update_application_prod_requests = context
                    .getConnection().prepareStatement(
                            "UPDATE application_prod_request SET DEPLOYMENT_PIPELINE_ID = "
                                    + pipelineId
                                    + " WHERE PROJECT_NAME = '"
                                    + projectName + "'")) {
                update_application_prod_requests.executeUpdate();
            }
        }
    }

    private void createDefaultEnvironmentsForReleasePipeline(Context context,
            int pipelineId) throws SQLException {
        String[] environments = new String[] { "uat" };
        for (int i = 0; i < environments.length; i++) {
            String environment = environments[i];
            try (PreparedStatement create_new_release_environment = context
                    .getConnection()
                    .prepareStatement(
                            "INSERT INTO release_deployment_environment(ID, DISPLAY_NAME, POSITION_IN_PIPELINE, POSTFIX, PIPELINE_ID) VALUES("
                                    + releaseEnvironmentIdCounter + ",'"
                                    + environment.toUpperCase() + "'," + i
                                    + ",'"
                                    + environment + "'," + pipelineId + ")")) {
                create_new_release_environment.executeUpdate();
                int environmentId = releaseEnvironmentIdCounter++;
                try (PreparedStatement associate_pipeline_to_project = context
                        .getConnection().prepareStatement(
                                "INSERT INTO release_deployment_pipeline_environments(RELEASE_DEPLOYMENT_PIPELINE_ENTITY_ID, ENVIRONMENTS_ID) VALUES("
                                        + pipelineId + ","
                                        + environmentId + ")")) {
                    associate_pipeline_to_project.executeUpdate();
                }
            }
        }
    }
}