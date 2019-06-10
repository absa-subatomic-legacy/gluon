package za.co.absa.subatomic.infrastructure.metadata;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "metadata")
@Setter(value = AccessLevel.PACKAGE)
@Getter
@Builder
public class MetadataEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String description;

    @OneToMany
    private List<MetadataEntry> metadataEntries;
}
