package za.co.absa.subatomic.adapter.project.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProjectResource extends ProjectResourceBase {

    private Date createdAt;

    private String createdBy;

    private BitbucketProjectResource bitbucketProject;

    private TeamResource owningTeam;

    private List<TeamResource> teams = new ArrayList<>();

    private ProjectEnvironment projectEnvironment;

    private DeploymentPipelineResource devDeploymentPipeline;

    private List<DeploymentPipelineResource> releaseDeploymentPipelines;
}
