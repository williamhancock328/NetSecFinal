package sse.client;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import sse.Token;

/**
 * Takes a list of keywords and converts it into a list of searchable tokens
 * @author Alex
 */
public class Tokenizer {
 
    /**
     * Protocol:
     * 
     * Tokens are constructed from a list of keywords that 
     *  - encrypted using the File_Password <Key,IV>
     *  - SHA-256 hashed using the encrypted keyword.
     * 
     */
    
    /**
     * Converts a list of plain-text keywords into Tokens from the derived hash of the session_key.
     * 
     * 
     * @param keywords List of keywords associated with a document 
     * @param key Key used to encrypt the tokens
     * @param Base64_IV IV used to encrypt the tokens
     * @return 
     */
    public static List<Token> tokenize(List<String> keywords, SecretKey key, String Base64_IV) {
        List<Token> ret = new ArrayList<>();
        
        IvParameterSpec IV = new IvParameterSpec(Base64.getDecoder().decode(Base64_IV));
        Cipher aesCipher = null;
        
        // Init the AES cipher
        try {
            // Use AES in GCM mode. No padding is needed as
            // GCM is a streaming AEAD mode.
            aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
            aesCipher.init(Cipher.ENCRYPT_MODE, key, IV);
        } 
        catch(NoSuchAlgorithmException nae)
        {
            nae.printStackTrace();
        }
        catch (InvalidKeyException ex) {
            Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Loop over each word
        keywords.forEach(word -> {
            // First encrypt the keyword with the key and Base64_IV pair
            // Then use SHA-256 to hash the encrypted string and add it to the return array as a Token object.
            try
            {
                // If the aesCipher object returned null w/o throwing any errors
                if(aesCipher == null)
                    return null;
                
                // Hash the keyword
                final byte[] hash = hash(word.getBytes());

                // AES encrypt hash
                final byte[] encrypted = aesCipher.doFinal(hash);
                                                
                // Add the hash to ret
                ret.add(new Token(Base64.getEncoder().encode(encrypted)));
            }
            catch (IllegalBlockSizeException ex) {
                Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BadPaddingException ex) {
                Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        return ret;
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
