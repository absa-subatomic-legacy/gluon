package za.co.absa.subatomic.infrastructure.application.view.jpa;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BitbucketRepositoryEmbedded {

    private String bitbucketId;

    private String slug;

    private String name;

    private String repoUrl;

    private String remoteUrl;
}
