package sse.client;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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
 * Used for Encryption and Decryption of File Names.
 * @author Alex
 */
public class FileNameSymmetricCrypto {
    
    /**
     * Encrypts a file name using a the file_password <Key,IV> pair
     * 
     * 
     * @param file File, a file as a byte[]. 
     * @param key Key used to encrypt the tokens
     * @param Base64_IV IV used to encrypt the tokens
     * @return a Base64 encoding of the file encrypted
     */
    public static String encryptFileName(String filename, SecretKey key, String Base64_IV) {
        
        IvParameterSpec IV = new IvParameterSpec(Base64.getDecoder().decode(Base64_IV));
        Cipher aesCipher = null;
        
        try {
            // Use AES in GCM mode. No padding is needed as
            // GCM is a streaming AEAD mode.
            aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
            aesCipher.init(Cipher.ENCRYPT_MODE, key, IV);
            
            // If the aesCipher object returned null w/o throwing any errors
            if(aesCipher == null)
                return null;
            
            // Encrypt the contents of the file with the Key and IV pair. 
            byte[] encrypted = aesCipher.doFinal(filename.getBytes());

            // Encode the Ek(file) into Base64 and return.
            return Base64.getEncoder().encodeToString(encrypted);
        } 
        catch(NoSuchAlgorithmException nae)
        {
            nae.printStackTrace();
        }
        catch (InvalidKeyException ex) {
            Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        return null;
    }
    
    /**
     * Decrypts a file using a the file_password <Key,IV> pair
     * 
     * @param cipher_text_file_name Encrypted File Name represented as the file text
     * @param key Key used to encrypt the tokens
     * @param Base64_IV IV used to encrypt the tokens
     * @return a Base64 encoding of the file name {@code cipher_text_file_name}
     */
    public static String decryptFileName(String cipher_text_file_name, SecretKey key, String Base64_IV) {
        
        IvParameterSpec IV = new IvParameterSpec(Base64.getDecoder().decode(Base64_IV));
        Cipher aesCipher = null;
        
        try {
            // Use AES in GCM mode. No padding is needed as
            // GCM is a streaming AEAD mode.
            aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
            aesCipher.init(Cipher.DECRYPT_MODE, key, IV);
            
            byte[] decoded_cipher_text = Base64.getDecoder().decode(cipher_text_file_name);
            
            // Decrypt the cipher text
            byte[] decrypted = aesCipher.doFinal(decoded_cipher_text);

            return Base64.getEncoder().encodeToString(decrypted);
        } 
        catch(NoSuchAlgorithmException nae)
        {
            nae.printStackTrace();
        }
        catch (InvalidKeyException ex) {
            Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        return null;
    }
    
}
