package za.co.absa.subatomic.infrastructure.atomist.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import za.co.absa.subatomic.domain.member.TeamMemberSlackIdentity;

@Data
@AllArgsConstructor
public class AtomistMemberBase {

    private String firstName;

    private String domainUsername;

    private TeamMemberSlackIdentity slackIdentity;
}