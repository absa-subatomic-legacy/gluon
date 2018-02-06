package za.co.absa.subatomic.domain.team;

import lombok.Value;

@Value
public class SlackIdentityAdded {

    private String teamId;

    private SlackIdentity slackIdentity;
}
