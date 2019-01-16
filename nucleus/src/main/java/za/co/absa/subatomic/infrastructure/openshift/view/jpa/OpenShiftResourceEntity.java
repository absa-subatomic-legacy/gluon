package za.co.absa.subatomic.infrastructure.openshift.view.jpa;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "openShiftResources")
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
public class OpenShiftResourceEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String openShiftResourceId;

    private String kind;

    private String name;

    @Convert(converter = EncryptedAttributeConverter.class)
    @Lob
    private String resourceDetails;
}
