package za.co.absa.subatomic.infrastructure.membership_request.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipRequestRepository extends JpaRepository<MembershipRequestEntity, Long> {

    MembershipRequestEntity findById(Long id);

    MembershipRequestEntity findByIdAndTeamId(Long id, String teamId);

    List<MembershipRequestEntity> findByTeamId(String teamId);

    List<MembershipRequestEntity> findByRequestedByFirstName(String name);

    List<MembershipRequestEntity> findByApprovedByFirstName(String name);
}
