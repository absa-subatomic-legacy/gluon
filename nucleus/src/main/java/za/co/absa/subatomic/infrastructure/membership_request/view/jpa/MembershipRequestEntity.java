package za.co.absa.subatomic.infrastructure.membership_request.view.jpa;

import lombok.*;
import za.co.absa.subatomic.domain.membership_request.MembershipRequestStatus;
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

    private String teamId;

    @OneToOne
    private TeamMemberEntity requestedBy;

    @OneToOne
    private TeamMemberEntity approvedBy;

    private MembershipRequestStatus requestStatus;

}
