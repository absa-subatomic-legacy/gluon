package za.co.absa.subatomic.domain.member;

import lombok.Value;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@Value
public class DomainCredentials {

    private String domain;

    private String username;

    private String password;

    public DomainCredentials(String domainUsername) {
        if (domainUsername.contains("\\")) {
            this.domain = substringBefore(domainUsername, "\\");
            this.username = substringAfter(domainUsername, "\\");
        }
        else {
            this.domain = null;
            this.username = domainUsername;
        }

        this.password = EMPTY;
    }
}
