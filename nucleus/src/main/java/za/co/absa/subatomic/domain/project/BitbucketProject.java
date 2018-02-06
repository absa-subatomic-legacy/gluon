package za.co.absa.subatomic.domain.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class BitbucketProject {

    private String id;

    private String key;

    private String name;

    private String description;

    private String url;
}
