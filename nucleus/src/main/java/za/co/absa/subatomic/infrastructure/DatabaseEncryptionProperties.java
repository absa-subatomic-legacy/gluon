package za.co.absa.subatomic.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("encryption")
public class DatabaseEncryptionProperties {

    private String encryptionKey;

}
