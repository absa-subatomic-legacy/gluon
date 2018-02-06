package za.co.absa.subatomic.adapter.pkg.rest;

import java.util.Date;

import lombok.Data;

import org.springframework.hateoas.ResourceSupport;

@Data
public class PackageResource extends ResourceSupport {

    private String applicationId;

    private String packageType;

    private String name;

    private String description;

    private Date createdAt;

    private String createdBy;

    private ProjectResource projectResource;
}
