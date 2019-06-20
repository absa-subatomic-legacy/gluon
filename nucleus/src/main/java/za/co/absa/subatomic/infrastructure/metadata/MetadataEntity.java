package za.co.absa.subatomic.infrastructure.metadata;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
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

    @OneToMany(cascade = CascadeType.ALL)
    private List<MetadataEntry> metadataEntries;
}
