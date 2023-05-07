package sse.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import merrimackutil.util.Tuple;
import org.bouncycastle.jcajce.spec.ScryptKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author Alex
 */
public class SecretKeyGenerator {
    
    /**
     * Derives the AES-128 key from the password.
     *
     * @param password the password to derive the key from
     * @return the AES-128 key
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static Tuple<SecretKey, String> genKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final int COST = 2048;          // A.K.A Iterations
        final int BLK_SIZE = 8;
        final int PARALLELIZATION = 1;  // Number of parallel threads to use.
        final int KEY_SIZE = 128;
        ScryptKeySpec scryptSpec;

        // Register bouncy castle provider.
        Security.addProvider(new BouncyCastleProvider());

        SecretKeyFactory factory = SecretKeyFactory.getInstance("SCRYPT");

        // Step 1 is to hash the password
        byte[] hash = hash(password.getBytes());
        
        
        // Step 2 is to take the first 16-bits of the hash and assign it to the salt / IV.
        byte[] salt = new byte[16];
        for(int i = 0; i < hash.length; i++) {
            // First 16 bits are the salt / IV
            if(i < 16) {
                salt[i] = hash[i];
            } else 
                break;
        }

        // Derive an AES key from the password using the password. The memory
        // required to run the derivation, in bytes, is:
        //    128 * COST * BLK_SIZE * PARALLELIZATION.
        // The password argument expects and array of charaters *not* bytes.
        //
        scryptSpec = new ScryptKeySpec(password.toCharArray(), salt, COST, BLK_SIZE,
                PARALLELIZATION, KEY_SIZE);

        // Generate the secrete key.
        SecretKey tmp = factory.generateSecret(scryptSpec);
        SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");
        return new Tuple(key, Base64.getEncoder().encode(salt));
    }
    
    /**
     * Used to SHA2-256 Hash the keyword
     * @param encrypted_keyword
     * @return 
     */
    private static byte[] hash(byte[] encrypted_keyword) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashbytes = digest.digest(encrypted_keyword);
        return hashbytes;
    }   
    
    
}
