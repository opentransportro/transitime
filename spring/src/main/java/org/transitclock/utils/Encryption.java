/* (C)2023 */
package org.transitclock.utils;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.StringConfigValue;

/**
 * For encrypting and decrypting strings.
 *
 * @author SkiBu Smith
 */
public class Encryption {

    private static final StringConfigValue encryptionPassword = new StringConfigValue(
            "transitclock.db.encryptionPassword",
            "SET THIS!",
            "Used for encrypting, deencrypting passwords for storage "
                    + "in a database. This value should be customized for each "
                    + "implementation and should be hidden from users.");

    // Must call getEncryptor() to initialize and access
    private static BasicTextEncryptor textEncryptor = null;

    private static final Logger logger = LoggerFactory.getLogger(Encryption.class);

    /********************** Member Functions **************************/

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
            logger.error("Problem decrypting the encrypted string " + encryptedStr);
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
            textEncryptor.setPassword(encryptionPassword.getValue());
        }

        return textEncryptor;
    }
}
