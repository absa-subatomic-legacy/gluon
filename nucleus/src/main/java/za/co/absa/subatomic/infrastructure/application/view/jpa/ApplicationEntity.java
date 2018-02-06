package za.co.absa.subatomic.infrastructure.application.view.jpa;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
import za.co.absa.subatomic.infrastructure.member.view.jpa.TeamMemberEntity;
import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;

@Entity
@Table(name = "application")
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
// https://stackoverflow.com/a/34299054/2408961
public class ApplicationEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String applicationId;

    private String name;

    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private final Date createdAt = new Date();

    @OneToOne
    private ProjectEntity project;

    @ManyToOne
    private TeamMemberEntity createdBy;
}
