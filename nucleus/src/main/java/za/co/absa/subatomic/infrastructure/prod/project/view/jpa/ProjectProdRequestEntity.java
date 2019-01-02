package za.co.absa.subatomic.infrastructure.prod.project.view.jpa;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.absa.subatomic.domain.prod.project.ProjectProductionRequestStatus;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ReleaseDeploymentPipelineEntity;

@Entity
@Table(name = "projectProdRequest")
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter
@Getter
public class ProjectProdRequestEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String projectProdRequestId;

    @ManyToOne
    private ProjectEntity project;

    @OneToOne
    private ReleaseDeploymentPipelineEntity deploymentPipeline;

    @OneToOne
    private TeamMemberEntity actionedBy;

    @ManyToMany
    private List<TeamMemberEntity> authorizingMembers = new ArrayList<>();

    @ManyToOne
    private TeamMemberEntity rejectingMember;

    @Temporal(TemporalType.TIMESTAMP)
    private final Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date closedAt;

    private String projectName;

    private ProjectProductionRequestStatus approvalStatus;

    public void close() {
        this.setClosedAt(Date.from(Instant.now()));
    }
}
