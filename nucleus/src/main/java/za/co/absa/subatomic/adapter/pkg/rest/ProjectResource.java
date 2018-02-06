package za.co.absa.subatomic.adapter.pkg.rest;

import lombok.Data;

import org.springframework.hateoas.ResourceSupport;

@Data
public class ProjectResource extends ResourceSupport {

    private String projectId;

    private String name;

}
