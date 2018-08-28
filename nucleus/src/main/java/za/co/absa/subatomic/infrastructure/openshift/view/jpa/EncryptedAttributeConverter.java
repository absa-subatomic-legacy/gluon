package za.co.absa.subatomic.infrastructure.openshift.view.jpa;

import java.io.ByteArrayOutputStream;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import za.co.absa.subatomic.domain.exception.AttributeDecryptionException;
import za.co.absa.subatomic.domain.exception.AttributeEncryptionException;
import za.co.absa.subatomic.infrastructure.DatabaseEncryptionProperties;

// See https://www.thoughts-on-java.org/how-to-use-jpa-type-converter-to/
// and https://stackoverflow.com/a/44891805/1630111
@Converter
@Component
@Configurable
@Slf4j
public class EncryptedAttributeConverter
        implements AttributeConverter<String, String> {

    private static DatabaseEncryptionProperties databaseEncryptionProperties;

    @Override
    public String convertToDatabaseColumn(String attributeValue) {

        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            SecretKeyFactory factory = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(this.getKey().toCharArray(), salt,
                    65536, 128);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            AlgorithmParameters params = cipher.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] encryptedText = cipher
                    .doFinal(attributeValue.getBytes("UTF-8"));

            // concatenate salt + iv + ciphertext
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(salt);
            outputStream.write(iv);
            outputStream.write(encryptedText);

            // properly encode the complete ciphertext
            return DatatypeConverter
                    .printBase64Binary(outputStream.toByteArray());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new AttributeEncryptionException(e.getMessage());
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            byte[] ciphertext = DatatypeConverter.parseBase64Binary(dbData);
            if (ciphertext.length < 48) {
                return null;
            }
            byte[] salt = Arrays.copyOfRange(ciphertext, 0, 16);
            byte[] iv = Arrays.copyOfRange(ciphertext, 16, 32);
            byte[] ct = Arrays.copyOfRange(ciphertext, 32, ciphertext.length);

            SecretKeyFactory factory = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(this.getKey().toCharArray(), salt,
                    65536, 128);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
            byte[] plaintext = cipher.doFinal(ct);

            return new String(plaintext, "UTF-8");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new AttributeDecryptionException(e.getMessage());
        }
    }

    @Autowired
    public void injectEncryptionProperties(
            DatabaseEncryptionProperties databaseEncryptionProperties) {
        EncryptedAttributeConverter.databaseEncryptionProperties = databaseEncryptionProperties;
    }

    private String getKey() {
        return databaseEncryptionProperties.getEncryptionKey();
    }
}