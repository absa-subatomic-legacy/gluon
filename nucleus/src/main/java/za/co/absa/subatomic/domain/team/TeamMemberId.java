package za.co.absa.subatomic.domain.team;

import lombok.Value;

@Value
public class TeamMemberId {

    private String teamMemberId;

    @Override
    public int hashCode() {
        return teamMemberId.hashCode();
    }

    public boolean equals(Object obj)
    {
        boolean isEqual = this == obj;
        if (obj != null && obj instanceof TeamMemberId){
            isEqual = this.teamMemberId.equals(((TeamMemberId) obj).teamMemberId);
        }
        return isEqual;
    }
}
