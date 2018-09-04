package za.co.absa.subatomic.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import lombok.Data;

@Data
@ConfigurationProperties("gluon")
public class GluonProperties {

    @NestedConfigurationProperty
    private DatabaseEncryptionProperties encryption;

    @NestedConfigurationProperty
    private ProjectProperties project;
}
