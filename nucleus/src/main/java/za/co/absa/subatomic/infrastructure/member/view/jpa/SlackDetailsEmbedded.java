package za.co.absa.subatomic.infrastructure.member.view.jpa;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SlackDetailsEmbedded {

    private String screenName;

    private String userId;
}
