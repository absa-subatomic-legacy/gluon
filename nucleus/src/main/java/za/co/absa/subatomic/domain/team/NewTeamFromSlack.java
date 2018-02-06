package za.co.absa.subatomic.domain.team;

import lombok.Value;

@Value
public class NewTeamFromSlack {

    private NewTeam basicTeamDetails;

    private SlackIdentity slackIdentity;
}
