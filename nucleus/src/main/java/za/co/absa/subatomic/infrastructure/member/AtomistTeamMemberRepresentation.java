package za.co.absa.subatomic.infrastructure.member;

import lombok.Builder;
import lombok.Value;
import za.co.absa.subatomic.domain.member.DomainCredentials;
import za.co.absa.subatomic.domain.member.TeamMemberSlack;

@Value
@Builder
class AtomistTeamMemberRepresentation {
    private String memberId;

    private String firstName;

    private String lastName;

    private String email;

    private DomainCredentials domainCredentials;

    private TeamMemberSlack slackIdentity;
}