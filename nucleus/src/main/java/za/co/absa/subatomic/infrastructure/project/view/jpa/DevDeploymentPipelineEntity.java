package za.co.absa.subatomic.infrastructure.project.view.jpa;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.absa.subatomic.domain.project.DeploymentPipeline;

@Entity
@Table(name = "devDeploymentPipeline")
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
public class DevDeploymentPipelineEntity implements DeploymentPipeline {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany
    private List<DevDeploymentEnvironmentEntity> environments;

    @Override
    public String getTag() {
        return "";
    }
}
