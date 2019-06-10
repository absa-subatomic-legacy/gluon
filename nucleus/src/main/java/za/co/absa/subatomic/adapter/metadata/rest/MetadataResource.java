package za.co.absa.subatomic.adapter.metadata.rest;

        import java.util.List;
        import org.springframework.hateoas.ResourceSupport;
        import lombok.Data;
        import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MetadataResource extends ResourceSupport {

    String description;

    List<MetadataEntryResource> metadataEntries;
}
