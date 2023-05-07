package sse;

import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import sse.client.Tokenizer;
import sse.files.Database;

/**
 * Implements the Searchable Symmetric Encryption algorithm.
 * @author William Hancock
 */
public class SSE {
    
    // List of the collection doccuments
    private static DocumentCollection docCollection = new DocumentCollection();
    // Database representing this class
    private static Database database;    
    
    /**
     * Cloud Server constructor
     */
    public SSE(String db_loc) {
        Setup(db_loc);
    }
    
    private void Setup(String db_loc){
        docCollection = new DocumentCollection(); // Init the DocumentCollection
        
        try {
            database = new Database(db_loc); // Construct the DB
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SSE.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidObjectException ex) {
            Logger.getLogger(SSE.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(database == null)
            throw new NullPointerException("Loading from a Null Database. \nIssue loading in the Database");
    }
    
    public List<Token> Token(SecretKey secretKey, String Base64_IV, List<String> keywords) {
        return Tokenizer.tokenize(keywords, secretKey, Base64_IV);
    }
    
    public List<EncryptedDocument>Search(Token tk){
        return docCollection.Search(tk);
    }
    
    public EncryptedDocument Insert(List<Token> tks, EncryptedDocument doc){
        return docCollection.Insert(tks, doc);
    }

   // public Token DeleteToken(Key secretKey, int id) {
   //     return null;
   // }

   // public ArrayList<EncryptedDocument> Delete(Token dtk){
   //     return null;
   // }

    /**
     * Accessors
     */
    
    /**
     * @return the docCollection
     */
    public static DocumentCollection getDocCollection() {
        return docCollection;
    }

    /**
     * @return the database
     */
    public static Database getDatabase() {
        return database;
    }
    
}
