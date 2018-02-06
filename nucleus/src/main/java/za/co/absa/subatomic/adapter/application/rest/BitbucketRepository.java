package za.co.absa.subatomic.adapter.application.rest;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BitbucketRepository {

    private String bitbucketId;

    private String name;

    private String repoUrl;

    private String remoteUrl;
}
