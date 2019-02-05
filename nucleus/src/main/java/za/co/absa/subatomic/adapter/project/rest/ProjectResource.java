package za.co.absa.subatomic.adapter.project.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import za.co.absa.subatomic.adapter.team.rest.TeamResourceBase;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProjectResource extends ProjectResourceBase {

    private Date createdAt;

    private String createdBy;

    private BitbucketProjectResource bitbucketProject;

    private TeamResourceBase owningTeam;

    private List<TeamResourceBase> teams = new ArrayList<>();

    private ProjectEnvironment projectEnvironment;

    private DeploymentPipelineResource devDeploymentPipeline;

    private List<DeploymentPipelineResource> releaseDeploymentPipelines;
}
