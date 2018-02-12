package za.co.absa.subatomic.infrastructure.team.view.jpa;

import lombok.*;
import za.co.absa.subatomic.domain.team.MembershipRequestStatus;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;

import javax.persistence.*;

@Entity
@Table(name = "membership_request")
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
public class MembershipRequestEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String membershipRequestId;

    private String teamId;

    @OneToOne
    private TeamMemberEntity requestedBy;

    @OneToOne
    private TeamMemberEntity approvedBy;

    private MembershipRequestStatus requestStatus;

}
