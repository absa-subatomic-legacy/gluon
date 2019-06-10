package za.co.absa.subatomic.adapter.metadata.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class MetadataEntryResource {

    private String key;
    private String value;
}
