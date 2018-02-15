package za.co.absa.subatomic.infrastructure.team.view.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.absa.subatomic.domain.team.MembershipRequestStatus;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;

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
