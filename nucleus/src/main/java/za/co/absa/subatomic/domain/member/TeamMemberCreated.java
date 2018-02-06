package za.co.absa.subatomic.domain.member;

import java.util.Optional;

import lombok.Value;
import lombok.experimental.NonFinal;

@Value
public class TeamMemberCreated {

    private String memberId;

    private String firstName;

    private String lastName;

    private String email;

    private DomainCredentials domainCredentials;

    @NonFinal
    private SlackIdentity slackIdentity;

    public TeamMemberCreated(String memberId,
            String firstName,
            String lastName,
            String email,
            DomainCredentials domainCredentials) {
        this.memberId = memberId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.domainCredentials = domainCredentials;
    }

    public TeamMemberCreated(String memberId,
            String firstName,
            String lastName,
            String email,
            DomainCredentials domainCredentials,
            SlackIdentity slackIdentity) {
        this(memberId, firstName, lastName, email, domainCredentials);
        this.slackIdentity = slackIdentity;
    }

    public Optional<SlackIdentity> getSlackIdentity() {
        return Optional.ofNullable(this.slackIdentity);
    }
}
