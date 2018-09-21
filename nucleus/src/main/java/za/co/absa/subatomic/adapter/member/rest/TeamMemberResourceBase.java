package za.co.absa.subatomic.adapter.member.rest;

import java.util.Date;

import org.springframework.hateoas.ResourceSupport;

import lombok.Data;
import lombok.EqualsAndHashCode;
import za.co.absa.subatomic.domain.member.TeamMember;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamMemberResourceBase extends ResourceSupport
        implements TeamMember {

    protected String memberId;

    protected String firstName;

    protected String lastName;

    protected String email;

    protected String domainUsername;

    protected Date joinedAt;

    protected Slack slack;
}
