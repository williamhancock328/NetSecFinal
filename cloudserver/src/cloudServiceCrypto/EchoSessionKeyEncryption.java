/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cloudServiceCrypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import merrimackutil.util.Tuple;

/**
 *
 * @author Mark Case
 */
public class EchoSessionKeyEncryption {
    public static byte[] rawIv;
    public static byte[] encrypt(byte[] sessKey, byte[] nonce, long valTime, long createTime, String uName, String sName) throws
            NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        
        int tagSize = 128;
        // Set up an AES cipher object.
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        //sessKey = Base64.getDecoder().decode(sessKey);
        SecretKey sessKey2 = new SecretKeySpec(sessKey, 0 , sessKey.length, "AES");
        // Get a key generator object.
        //KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");

        // Set the key size to 128 bits.
        //aesKeyGen.init(128);
        
        //System.out.println("MKEY: " + mkey);

        // Generate the session key.
        //SecretKey aesKey = aesKeyGen.generateKey();
        //generate the master key from the password.
        //SecretKey mKey = scrypt.genKey(mkey, uName);
        //System.out.println("SCRYPT ENCRYPT: " + mKey);
        // Generate the IV.
        SecureRandom rand = new SecureRandom();
        rawIv = new byte[16];		// Block size of AES.
        rand.nextBytes(rawIv);					// Fill the array with random bytes.
        //System.out.println(mkey+valTime+createTime+uName+Base64.getEncoder().encodeToString(rawIv));
        GCMParameterSpec gcmParams = new GCMParameterSpec(tagSize, rawIv);

        // Put the cipher in encrypt mode with the specified key.
        aesCipher.init(Cipher.ENCRYPT_MODE, sessKey2, gcmParams);
        aesCipher.updateAAD(sName.getBytes(StandardCharsets.UTF_8));
        //encrypt the session key
        byte[] ciphertext = aesCipher.doFinal(nonce);
        return ciphertext;
        
    }

    public static byte[] getRawIv() {
        return rawIv;
    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
    
}
