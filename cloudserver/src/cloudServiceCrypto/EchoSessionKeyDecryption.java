/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cloudServiceCrypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Mark Case
 */
public class EchoSessionKeyDecryption {
    
            public static byte[] decrypt(String ct, String IV, String uName, byte[] sessKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
        //String masterPass = "";

        int tagSize = 128; // 128-bit authentication tag.


        // Set up an AES cipher object.
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");

        SecretKey sessKey2 = new SecretKeySpec(sessKey, 0 , sessKey.length, "AES");
        String encodedKey = Base64.getEncoder().encodeToString(sessKey2.getEncoded());
        System.out.println("echo sess key 2: " + sessKey2);
        // Setup the key.
        //SecretKeySpec aesKey = new SecretKeySpec(keyBytes, "AES");
        try {
            // Put the cipher in encrypt mode with the specified key.
            aesCipher.init(Cipher.DECRYPT_MODE, sessKey2, new GCMParameterSpec(tagSize, Base64.getDecoder().decode(IV)));
            aesCipher.updateAAD(uName.getBytes(StandardCharsets.UTF_8));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(EchoSessionKeyDecryption.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Finalize the message.
        byte[] plaintext = null;
        try {
            plaintext = aesCipher.doFinal(Base64.getDecoder().decode(ct));
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(EchoSessionKeyDecryption.class.getName()).log(Level.SEVERE, null, ex);
        }
        return plaintext;

    } // End 'decrypt' method


    
}