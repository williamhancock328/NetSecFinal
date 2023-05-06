package sse;

import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import java.security.Key;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    public Token Token(Key secretKey, String keyword) {
        
    }
    
    public ArrayList<EncryptedDocument> Search (Token tk){
        
    }
    
    public Token InsertToken(Key secretKey, String keyword) {
        
    }

    public ArrayList<EncryptedDocument> Insert (Token tk){
        
    }

    public Token DeleteToken(Key secretKey, int id) {
        
    }

    public ArrayList<EncryptedDocument> Delete (Token dtk){
        
    }

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
