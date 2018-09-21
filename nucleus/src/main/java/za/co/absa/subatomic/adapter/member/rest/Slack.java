package za.co.absa.subatomic.adapter.member.rest;

import lombok.Value;
import za.co.absa.subatomic.domain.member.TeamMemberSlack;

@Value
public class Slack implements TeamMemberSlack {

    private String screenName;

    private String userId;
}
