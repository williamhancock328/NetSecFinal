package sse;

import java.security.Key;
import java.util.ArrayList;

/**
 * Implements the Searchable Symmetric Encryption algorithm.
 * @author William Hancock
 */
public class SSE {
    
    // List of the collection doccuments
    private DocCollection docCollection;
        
    /**
     * Default constructor
     */
    public SSE(String db_loc) {
        
        Setup();
    }
    
    private void Setup(){
        
    }
    
    public Token Token(Key secretKey, String keyword) {
        
    }
    
    public ArrayList<EncryptedDocument> Search (int encIdx, DocCollection dc, Token tk){
        
    }
    
    public Token InsertToken(Key secretKey, String keyword) {
        
    }

    public ArrayList<EncryptedDocument> Insert (int encIdx, DocCollection dc, Token tk){
        
    }

    public Token DeleteToken(Key secretKey, int id) {
        
    }

    public ArrayList<EncryptedDocument> Delete (DocCollection dc, Token dtk){
        
    }
    
}
