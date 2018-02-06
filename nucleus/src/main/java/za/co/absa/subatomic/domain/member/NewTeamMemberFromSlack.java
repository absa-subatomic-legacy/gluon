package za.co.absa.subatomic.domain.member;

import lombok.Value;

@Value
public class NewTeamMemberFromSlack {

    private NewTeamMember basicNewTeamMember;

    private SlackIdentity slackIdentity;
}
