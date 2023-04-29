package twoauth;

import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.security.Security;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import merrimackutil.util.Tuple;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jcajce.spec.ScryptKeySpec;

/**
 * class that derives the AES-128 key from a password
 *
 * @author willi
 */
public class scrypt {

    /**
     * Derives the AES-128 key from the password.
     * @param password the password to derive the key from
     * @return the AES-128 key, and salt
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static Tuple<SecretKey,byte[]> genKey(String password) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        final int COST = 2048;          // A.K.A Iterations
        final int BLK_SIZE = 8;
        final int PARALLELIZATION = 1;  // Number of parallel threads to use.
        final int KEY_SIZE = 128;
        ScryptKeySpec scryptSpec;

        // Register bouncy castle provider.
        Security.addProvider(new BouncyCastleProvider());

        SecretKeyFactory factory = SecretKeyFactory.getInstance("SCRYPT");

        // Get a 16-byte salt.
            byte[] salt = new byte[16];
            SecureRandom rand = new SecureRandom();
            rand.nextBytes(salt);

        // Derive an AES key from the password using the password. The memory
        // required to run the derivation, in bytes, is:
        //    128 * COST * BLK_SIZE * PARALLELIZATION.
        // The password argument expects and array of charaters *not* bytes.
        //
        scryptSpec = new ScryptKeySpec(password.toCharArray(), salt, COST, BLK_SIZE,
                PARALLELIZATION, KEY_SIZE);

        // Generate the secrete key.
        SecretKey key = factory.generateSecret(
                scryptSpec);
        Tuple<SecretKey,byte[]> ret = new Tuple<>(key,salt);
        return ret;

    }

    /**
     * Derives the AES-128 key from the password and a salt.
     * @param password the password to derive the key from
     * @param salt the salt to use
     * @return the AES-128 key
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static String checkPw(String password, byte[] salt) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        final int COST = 2048;          // A.K.A Iterations
        final int BLK_SIZE = 8;
        final int PARALLELIZATION = 1;  // Number of parallel threads to use.
        final int KEY_SIZE = 128;
        ScryptKeySpec scryptSpec;

        // Register bouncy castle provider.
        Security.addProvider(new BouncyCastleProvider());

        SecretKeyFactory factory = SecretKeyFactory.getInstance("SCRYPT");

        // Derive an AES key from the password using the password. The memory
        // required to run the derivation, in bytes, is:
        //    128 * COST * BLK_SIZE * PARALLELIZATION.
        // The password argument expects and array of charaters *not* bytes.
        //
        scryptSpec = new ScryptKeySpec(password.toCharArray(), salt, COST, BLK_SIZE,
                PARALLELIZATION, KEY_SIZE);

        // Generate the secrete key.
        SecretKey key = factory.generateSecret(
                scryptSpec);
        return Base64.getEncoder().encodeToString(key.getEncoded());

    }
    
}
