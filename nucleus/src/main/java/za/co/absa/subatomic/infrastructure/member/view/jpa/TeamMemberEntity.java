package za.co.absa.subatomic.infrastructure.member.view.jpa;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.absa.subatomic.infrastructure.team.view.jpa.TeamEntity;

@Entity
@Table(name = "team_member")
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
// https://stackoverflow.com/a/34299054/2408961
public class TeamMemberEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String memberId;

    private String firstName;

    private String lastName;

    private String email;

    private String domainUsername;

    @Temporal(TemporalType.TIMESTAMP)
    private final Date joinedAt = new Date();

    @ManyToMany
    private final Set<TeamEntity> teams = new HashSet<>();

    @Embedded
    private SlackDetailsEmbedded slackDetails;

}
