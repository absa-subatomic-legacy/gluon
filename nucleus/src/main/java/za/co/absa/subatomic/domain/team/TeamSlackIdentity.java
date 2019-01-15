package za.co.absa.subatomic.domain.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TeamSlackIdentity implements TeamSlack {

    /**
     * The Slack channel name for this team. If no existing channel is chosen and a new Slack
     * channel is created, then this value is normally the kebab case of the
     * <code>Team#name</code>.
     */
    private String teamChannel;
}
