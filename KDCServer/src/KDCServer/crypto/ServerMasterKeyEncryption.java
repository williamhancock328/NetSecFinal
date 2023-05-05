package KDCServer.crypto;

import java.nio.ByteBuffer;
import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.InvalidKeyException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.security.InvalidAlgorithmParameterException;
import java.nio.charset.StandardCharsets;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import merrimackutil.util.Tuple;

public class ServerMasterKeyEncryption {

    /**
     * 
     * @param mkey master scrypt
     * @param valTime aad
     * @param createTime aad
     * @param uName aad
     * @param sName aad
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeySpecException 
     */
    public static Tuple<byte[], byte[]> encrypt(String mkey, long valTime, long createTime, String uName, String sName, String user, SecretKey aesKey) throws
            NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        
        int tagSize = 128;
        // Set up an AES cipher object.
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");

        //generate the master key from the password.
        SecretKey mKey = scrypt.genKey(mkey, user);
        System.out.println("SCRYPT ENCRYPT: " + mKey);
        // Generate the IV.
        SecureRandom rand = new SecureRandom();
        byte[] rawIv = new byte[16];		// Block size of AES.
        rand.nextBytes(rawIv);					// Fill the array with random bytes.
        System.out.println(mkey+valTime+createTime+uName+Base64.getEncoder().encodeToString(rawIv));
        GCMParameterSpec gcmParams = new GCMParameterSpec(tagSize, rawIv);

        // Put the cipher in encrypt mode with the specified key.
        aesCipher.init(Cipher.ENCRYPT_MODE, mKey, gcmParams);
        aesCipher.updateAAD(longToBytes(createTime));
        aesCipher.updateAAD(longToBytes(valTime));
        aesCipher.updateAAD(uName.getBytes(StandardCharsets.UTF_8));
        aesCipher.updateAAD(sName.getBytes(StandardCharsets.UTF_8));
        //encrypt the session key
        byte[] ciphertext = aesCipher.doFinal(aesKey.getEncoded());
        return new Tuple<byte[], byte[]>(ciphertext, rawIv);
        
    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
}
