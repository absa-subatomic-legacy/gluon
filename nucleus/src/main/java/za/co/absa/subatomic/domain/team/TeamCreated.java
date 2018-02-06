package za.co.absa.subatomic.domain.team;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.Value;
import lombok.experimental.NonFinal;

@Value
public class TeamCreated {

    private String teamId;

    private String name;

    private String description;

    private final TeamMemberId createdBy;

    private Set<TeamMemberId> owners = new HashSet<>();

    private Set<TeamMemberId> members = new HashSet<>();

    @NonFinal
    private SlackIdentity slackIdentity;

    public TeamCreated(String teamId,
            String name,
            String description,
            TeamMemberId createdBy) {
        this.teamId = teamId;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }

    public TeamCreated(String teamId,
            String name,
            String description,
            TeamMemberId createdBy,
            SlackIdentity slackIdentity) {
        this(teamId, name, description, createdBy);
        this.slackIdentity = slackIdentity;
    }

    public Optional<SlackIdentity> getSlackIdentity() {
        return Optional.ofNullable(this.slackIdentity);
    }
}
