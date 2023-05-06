/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ClientServerCrypto;

import java.io.IOException;
import java.nio.ByteBuffer;
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
 * @author MarkC
 */
public class ClientMasterKeyDecryption {

    public static byte[] decrypt(String ct, String IV, String uName, String mKey, long createTime, long valTime, String sessName) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
        //String masterPass = "";
       // System.out.println(mKey + valTime + createTime + uName + IV);
        int tagSize = 128; // 128-bit authentication tag.
        SecretKey key = scrypt.genKey(mKey, uName);
        //byte[] keyBytes = key.getEncoded();
        //.out.println("SCRYPT DECRYPT: " + key);

        // Set up an AES cipher object.
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");

        // Setup the key.
        //SecretKeySpec aesKey = new SecretKeySpec(keyBytes, "AES");
        try {
            // Put the cipher in encrypt mode with the specified key.
            aesCipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(tagSize, Base64.getDecoder().decode(IV)));
            aesCipher.updateAAD(longToBytes(createTime));
            aesCipher.updateAAD(longToBytes(valTime));
            aesCipher.updateAAD(uName.getBytes(StandardCharsets.UTF_8));
            aesCipher.updateAAD(sessName.getBytes(StandardCharsets.UTF_8));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(ClientMasterKeyDecryption.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Finalize the message.
        byte[] plaintext = null;
        try {
            plaintext = aesCipher.doFinal(Base64.getDecoder().decode(ct));
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(ClientMasterKeyDecryption.class.getName()).log(Level.SEVERE, null, ex);
        }
        return plaintext;

    } // End 'decrypt' method

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
}
