package za.co.absa.subatomic.adapter.application.rest;

import java.util.Date;

import lombok.Data;

import org.springframework.hateoas.ResourceSupport;

@Data
public class ApplicationResource extends ResourceSupport {

    private String applicationId;

    private String name;

    private String description;

    private String applicationType;

    private String projectId;

    private Date createdAt;

    private String createdBy;

    private BitbucketRepository bitbucketRepository;
}
