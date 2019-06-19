package za.co.absa.subatomic.infrastructure.metadata;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Entity
@Table(name = "metadataEntries")
@Setter
@Getter
@Builder
public class MetadataEntry {
    @Id
    @GeneratedValue
    private Long id;

    private String key;
    private String value;
}
