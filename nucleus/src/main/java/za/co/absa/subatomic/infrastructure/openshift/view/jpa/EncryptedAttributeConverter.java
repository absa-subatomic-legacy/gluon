package za.co.absa.subatomic.infrastructure.openshift.view.jpa;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import za.co.absa.subatomic.domain.exception.AttributeDecryptionException;
import za.co.absa.subatomic.domain.exception.AttributeEncryptionException;

@Converter
//See https://www.thoughts-on-java.org/how-to-use-jpa-type-converter-to/
public class EncryptedAttributeConverter
        implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    private static final byte[] KEY = "MySuperSecretKey".getBytes();

    @Override
    public String convertToDatabaseColumn(String ccNumber) {
        Key key = new SecretKeySpec(KEY, "AES");
        try {
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder()
                    .encodeToString(c.doFinal(ccNumber.getBytes()));
        }
        catch (Exception e) {
            throw new AttributeEncryptionException(e.getMessage());
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        Key key = new SecretKeySpec(KEY, "AES");
        try {
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, key);
            return new String(c.doFinal(Base64.getDecoder().decode(dbData)));
        }
        catch (Exception e) {
            throw new AttributeDecryptionException(e.getMessage());
        }
    }
}