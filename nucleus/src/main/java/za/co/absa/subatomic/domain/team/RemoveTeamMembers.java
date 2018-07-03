package za.co.absa.subatomic.domain.team;

import java.util.List;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class RemoveTeamMembers {

    @TargetAggregateIdentifier
    private String teamId;

    private TeamMemberId actionedBy;

    private List<String> ownerMemberIds;

    private List<String> teamMemberIds;

}
