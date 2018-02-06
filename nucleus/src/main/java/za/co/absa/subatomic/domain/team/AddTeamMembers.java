package za.co.absa.subatomic.domain.team;

import java.util.List;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class AddTeamMembers {

    @TargetAggregateIdentifier
    private String teamId;

    private List<String> ownerMemberIds;

    private List<String> teamMemberIds;
}
