package za.co.absa.subatomic.infrastructure.team.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRequestRepository
        extends JpaRepository<MembershipRequestEntity, Long> {

    MembershipRequestEntity findByMembershipRequestId(
            String membershipRequestId);
}
