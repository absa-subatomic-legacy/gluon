package za.co.absa.subatomic.adapter.member.rest;

import java.util.Date;

import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

import lombok.Data;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamMemberResourceBase extends ResourceSupport {

    protected String memberId;

    protected String firstName;

    protected String lastName;

    protected String email;

    protected String domainUsername;

    protected Date joinedAt;

    protected Slack slack;
}
