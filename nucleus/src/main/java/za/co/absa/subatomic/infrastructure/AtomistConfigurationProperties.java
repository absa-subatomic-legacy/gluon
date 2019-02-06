package za.co.absa.subatomic.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("atomist")
public class AtomistConfigurationProperties {

    private String slackIdentityUrl;

    private String gluonTeamUrl;

    private String teamCreatedEventUrl;

    private String teamMemberCreatedEventUrl;

    private String projectCreatedEventUrl;

    private String bitbucketProjectRequestedEventUrl;

    private String bitbucketProjectAddedEventUrl;

    private String devOpsEnvironmentRequestedEventUrl;

    private String projectEnvironmentsRequestedEventUrl;

    private String projectProductionEnvironmentsRequestedEventUrl;

    private String projectProductionEnvironmentsRequestClosedEventUrl;

    private String applicationCreatedEventUrl;

    private String membershipRequestCreatedEventUrl;

    private String membersAddedToTeamEventUrl;

    private String memberRemovedFromTeamEventUrl;

    private String teamsLinkedToProjectEventUrl;

    private String applicationProdRequestedEventUrl;

    private String genericProdRequestedEventUrl;

    private String teamOpenShiftCloudMigratedEventUrl;
}
