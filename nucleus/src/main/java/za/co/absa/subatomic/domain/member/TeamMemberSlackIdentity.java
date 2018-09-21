package za.co.absa.subatomic.domain.member;

import lombok.Value;

@Value
public class TeamMemberSlackIdentity implements TeamMemberSlack {

    private String screenName;

    private String userId;
}
