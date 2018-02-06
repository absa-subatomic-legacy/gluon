package za.co.absa.subatomic.domain.member;

import lombok.Value;

@Value
public class SlackIdentityAdded {

    private String memberId;

    private String screenName;

    private String userId;
}
