package za.co.absa.subatomic.adapter.prod.project.rest;

import java.util.Date;
import java.util.List;

import org.springframework.hateoas.ResourceSupport;

import lombok.Data;
import lombok.EqualsAndHashCode;
import za.co.absa.subatomic.adapter.member.rest.TeamMemberResourceBase;
import za.co.absa.subatomic.adapter.project.rest.DeploymentPipelineResource;
import za.co.absa.subatomic.adapter.project.rest.ProjectResourceBase;
import za.co.absa.subatomic.domain.prod.project.ProjectProductionRequestStatus;
import za.co.absa.subatomic.domain.project.DeploymentPipeline;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProjectProdRequestResource extends ResourceSupport {

    private String projectProdRequestId;

    private ProjectResourceBase project;

    private DeploymentPipelineResource deploymentPipeline;

    private Date createdAt;

    private Date closedAt;

    private TeamMemberResourceBase actionedBy;

    private List<TeamMemberResourceBase> authorizingMembers;

    private TeamMemberResourceBase rejectingMember;

    private ProjectProductionRequestStatus approvalStatus;
}
