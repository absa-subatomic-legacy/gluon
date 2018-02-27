package za.co.absa.subatomic.adapter.project.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

import org.springframework.hateoas.ResourceSupport;

@Data
public class ProjectResource extends ResourceSupport {

    private String projectId;

    private String name;

    private String description;

    private Date createdAt;

    private String createdBy;

    private BitbucketProjectResource bitbucketProject;

    private TeamResource owningTeam;

    private List<TeamResource> teams = new ArrayList<>();

    private ProjectEnvironment projectEnvironment;
}
