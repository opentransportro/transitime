/* (C)2023 */
package org.transitclock.utils;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.transitclock.config.data.DbSetupConfig;

/**
 * For encrypting and decrypting strings.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class Encryption {

    // Must call getEncryptor() to initialize and access
    private static BasicTextEncryptor textEncryptor = null;

    /**
     * Encrypts the specified string using the configured encryptionPassword
     *
     * @param str String to be encrypted
     * @return The encrypted string
     */
    public static String encrypt(String str) {
        return getEncryptor().encrypt(str);
    }

    /**
     * Decrypts the encrypted string using the configured encryptionPassword
     *
     * @param encryptedStr The string to decrypt
     * @throws EncryptionOperationNotPossibleException When the encryptionPassword is not correct
     * @return The decrypted string
     */
    public static String decrypt(String encryptedStr) throws EncryptionOperationNotPossibleException {
        try {
            return getEncryptor().decrypt(encryptedStr);
        } catch (EncryptionOperationNotPossibleException e) {
            logger.error("Problem decrypting the encrypted string {}", encryptedStr);
            throw e;
        }
    }

    /**
     * Creates the BasicTextEncryptor textEncryptor member and sets the password
     *
     * @return A BasicTextEncryptor with the password set
     */
    private static TextEncryptor getEncryptor() {
        if (textEncryptor == null) {
            textEncryptor = new BasicTextEncryptor();
            textEncryptor.setPassword(DbSetupConfig.encryptionPassword.getValue());
        }

        return textEncryptor;
    }
}
