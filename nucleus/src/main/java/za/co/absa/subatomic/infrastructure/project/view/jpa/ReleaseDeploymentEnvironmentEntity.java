package za.co.absa.subatomic.infrastructure.project.view.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.absa.subatomic.domain.project.DeploymentEnvironment;

@Entity
@Table(name = "releaseDeploymentEnvironment")
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
class ReleaseDeploymentEnvironmentEntity
        implements DeploymentEnvironment {

    @Id
    @GeneratedValue
    private Long id;

    private int positionInPipeline;

    private String displayName;

    private String prefix;

    @ManyToOne
    private ReleaseDeploymentPipelineEntity pipeline;
}
