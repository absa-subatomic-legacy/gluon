package za.co.absa.subatomic.infrastructure.member.view.jpa;

import org.axonframework.eventhandling.EventHandler;
import za.co.absa.subatomic.domain.member.SlackIdentityAdded;
import za.co.absa.subatomic.domain.member.TeamMemberCreated;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TeamMemberHandler {

    private TeamMemberRepository teamMemberRepository;

    public TeamMemberHandler(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    @EventHandler
    @Transactional
    void on(TeamMemberCreated event) {
        TeamMemberEntity teamMemberEntity = TeamMemberEntity.builder()
                .memberId(event.getMemberId())
                .firstName(event.getFirstName())
                .lastName(event.getLastName())
                .email(event.getEmail())
                .domainUsername(String.format("%s\\%s",
                        event.getDomainCredentials().getDomain(),
                        event.getDomainCredentials().getUsername()))
                .build();

        event.getSlackIdentity()
                .ifPresent(slackIdentity -> teamMemberEntity.setSlackDetails(
                        new SlackDetailsEmbedded(
                                slackIdentity.getScreenName(),
                                slackIdentity.getUserId())));

        teamMemberRepository.save(teamMemberEntity);
    }

    @EventHandler
    @Transactional
    void on(SlackIdentityAdded event) {
        TeamMemberEntity teamMember = teamMemberRepository
                .findByMemberId(event.getMemberId());
        teamMember.setSlackDetails(new SlackDetailsEmbedded(
                event.getScreenName(),
                event.getUserId()));
        teamMemberRepository.save(teamMember);
    }
}
