package za.co.absa.subatomic.domain.team;

import lombok.Value;

@Value
public class MembershipRequestCreated {

    private String teamId;

    private MembershipRequest membershipRequest;

}
