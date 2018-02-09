package za.co.absa.subatomic.infrastructure.team.view.jpa;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.absa.subatomic.domain.team.MembershipRequestStatus;
import za.co.absa.subatomic.domain.team.NewMembershipRequest;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberRepository;

@Component
public class MembershipRequestHandler {

    private MembershipRequestRepository membershipRequestRepository;

    private TeamMemberRepository teamMemberRepository;

    public MembershipRequestHandler(MembershipRequestRepository membershipRequestRepository,
                                    TeamMemberRepository teamMemberRepository) {
        this.membershipRequestRepository = membershipRequestRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @EventHandler
    @Transactional
    void on(NewMembershipRequest event) {
        TeamMemberEntity requestedBy = teamMemberRepository
                .findByMemberId(event.getRequestedBy().getTeamMemberId());

        MembershipRequestEntity membershipRequestEntity = MembershipRequestEntity.builder()
                .teamId(event.getTeamId())
                .requestedBy(requestedBy)
                .requestStatus(MembershipRequestStatus.OPEN)
                .build();

        membershipRequestRepository.save(membershipRequestEntity);
    }

}
