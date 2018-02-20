package za.co.absa.subatomic.domain.application;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BitbucketGitRepository {

    private String bitbucketId;

    private String slug;

    private String name;

    private String repoUrl;

    private String remoteUrl;
}
