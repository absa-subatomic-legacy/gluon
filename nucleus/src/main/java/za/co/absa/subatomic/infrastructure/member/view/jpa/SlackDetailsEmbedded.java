package za.co.absa.subatomic.infrastructure.member.view.jpa;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.absa.subatomic.domain.member.TeamMemberSlack;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SlackDetailsEmbedded implements TeamMemberSlack {

    private String screenName;

    private String userId;
}
