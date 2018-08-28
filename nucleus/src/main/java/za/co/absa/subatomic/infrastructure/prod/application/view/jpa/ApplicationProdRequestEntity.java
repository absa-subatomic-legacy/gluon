package za.co.absa.subatomic.infrastructure.prod.application.view.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
import za.co.absa.subatomic.infrastructure.application.view.jpa.ApplicationEntity;
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.openshift.view.jpa.OpenShiftResourceEntity;

@Entity
@Table(name = "applicationProdRequest")
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
// https://stackoverflow.com/a/34299054/2408961
public class ApplicationProdRequestEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String applicationProdRequestId;

    @ManyToOne
    private ApplicationEntity application;

    @OneToOne
    private TeamMemberEntity actionedBy;

    @Temporal(TemporalType.TIMESTAMP)
    private final Date createdAt = new Date();

    private String applicationName;

    private String projectName;

    @OneToMany
    private List<OpenShiftResourceEntity> openshiftResources;
}
