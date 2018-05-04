package za.co.absa.subatomic.infrastructure.tenant.view.jpa;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;

@Entity
@Table(name = "tenant")
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
// https://stackoverflow.com/a/34299054/2408961
public class TenantEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String tenantId;

    private String name;

    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private final Date createdAt = new Date();

    @OneToMany
    private Set<ProjectEntity> projects = new HashSet<>();

}
