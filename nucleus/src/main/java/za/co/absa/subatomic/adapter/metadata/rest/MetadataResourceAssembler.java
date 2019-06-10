package za.co.absa.subatomic.adapter.metadata.rest;

import za.co.absa.subatomic.infrastructure.metadata.MetadataEntity;

import java.util.List;
import java.util.stream.Collectors;

public class MetadataResourceAssembler{

    public MetadataResource toResource(
            MetadataEntity entity) {
        if (entity != null) {
            MetadataResource resource = new MetadataResource();
            resource.setDescription(entity.getDescription());
            List<MetadataEntryResource> metadataResources =
            entity.getMetadataEntries().stream().map(metadataEntry -> new MetadataEntryResource(metadataEntry.getKey(), metadataEntry.getValue()
                    )).collect(Collectors.toList());
            resource.setMetadataEntries(metadataResources);

            return resource;
        } else {
            return null;
        }
    }
}
