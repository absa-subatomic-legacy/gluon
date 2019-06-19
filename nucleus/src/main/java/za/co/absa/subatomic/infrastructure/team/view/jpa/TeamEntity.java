package za.co.absa.subatomic.infrastructure.team.view.jpa;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.metadata.MetadataEntity;

@Entity
@Table(name = "team")
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
// https://stackoverflow.com/a/34299054/2408961
public class TeamEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String teamId;

    private String name;

    private String description;

    private String openShiftCloud;

    @Temporal(TemporalType.TIMESTAMP)
    private final Date createdAt = new Date();

    @ManyToOne
    private TeamMemberEntity createdBy;

    @Embedded
    private SlackDetailsEmbedded slackDetails;

    @ManyToMany
    private Set<TeamMemberEntity> members = new HashSet<>();

    @ManyToMany
    private Set<TeamMemberEntity> owners = new HashSet<>();

    @OneToMany
    private Set<MembershipRequestEntity> membershipRequests = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    private List<MetadataEntity> metadata;

}
