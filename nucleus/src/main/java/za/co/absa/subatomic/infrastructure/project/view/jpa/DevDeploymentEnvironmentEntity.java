package za.co.absa.subatomic.infrastructure.project.view.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.absa.subatomic.domain.project.DeploymentEnvironment;

@Entity
@Table(name = "devDeploymentEnvironment", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "id", "position_in_pipeline" }),
        @UniqueConstraint(columnNames = { "id", "display_name" }),
        @UniqueConstraint(columnNames = { "id", "postfix" }) })
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
public class DevDeploymentEnvironmentEntity implements DeploymentEnvironment {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "position_in_pipeline")
    private int positionInPipeline;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "postfix")
    private String postfix;

    @ManyToOne
    private DevDeploymentPipelineEntity pipeline;
}
