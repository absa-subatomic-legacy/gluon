package za.co.absa.subatomic.adapter.application.rest;

import java.util.Date;

import org.springframework.hateoas.ResourceSupport;

import lombok.Data;
import lombok.EqualsAndHashCode;
import za.co.absa.subatomic.adapter.member.rest.TeamMemberResourceBase;
import za.co.absa.subatomic.domain.application.Application;
import za.co.absa.subatomic.domain.application.ApplicationType;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApplicationResource extends ResourceSupport
        implements Application {

    private String applicationId;

    private String name;

    private String description;

    private String jenkinsFolder;

    private ApplicationType applicationType;

    private String projectId;

    private Date createdAt;

    private TeamMemberResourceBase createdBy;

    private BitbucketRepository bitbucketRepository;

    private Boolean requestConfiguration;
}
