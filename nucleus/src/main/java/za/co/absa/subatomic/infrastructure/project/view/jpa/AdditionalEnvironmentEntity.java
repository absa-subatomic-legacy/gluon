package za.co.absa.subatomic.infrastructure.project.view.jpa;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "AdditionalEnvironment", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"display_name", "owning_project"})
})
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
public class AdditionalEnvironmentEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "display_name")
    private String displayName;

    @ManyToOne
    @JoinColumn(name = "owning_project")
    private ProjectEntity owningProject;
}
