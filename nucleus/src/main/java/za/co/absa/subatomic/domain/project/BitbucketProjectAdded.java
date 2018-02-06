package za.co.absa.subatomic.domain.project;

import lombok.Value;
import za.co.absa.subatomic.domain.pkg.ProjectId;

@Value
public class BitbucketProjectAdded {

    private ProjectId projectId;

    private BitbucketProject bitbucketProject;
}
