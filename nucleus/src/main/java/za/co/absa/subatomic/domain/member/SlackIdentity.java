package za.co.absa.subatomic.domain.member;

import lombok.Value;

@Value
public class SlackIdentity {

    private String screenName;

    private String userId;
}
