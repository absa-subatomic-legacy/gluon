package za.co.absa.subatomic.infrastructure;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("atomist")
public class AtomistConfigurationProperties {

    private String slackIdentityUrl;

    private String teamUrl;

    private String teamCreatedEventUrl;

    private String teamMemberCreatedEventUrl;

    private String projectCreatedEventUrl;

    private String bitbucketProjectRequestedEventUrl;

    private String bitbucketProjectAddedEventUrl;

    private String devOpsEnvironmentRequestedEventUrl;

    private String projectEnvironmentsRequestedEventUrl;

    private String applicationCreatedEventUrl;
}
