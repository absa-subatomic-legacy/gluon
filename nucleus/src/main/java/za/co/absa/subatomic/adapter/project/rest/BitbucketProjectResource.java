package za.co.absa.subatomic.adapter.project.rest;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BitbucketProjectResource {

    private String bitbucketProjectId;

    private String key;

    private String name;

    private String description;

    private String url;
}
