
package sse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import sse.files.Entry;

/**
 *
 * @author William Hancock
 */
public class DocumentCollection {

    /**
     * Index Table HashMap
     * 
     * Each (Token,Document) pair represents a token pointing towards a document in the cloud server
     */
    private HashMap<List<Token>, EncryptedDocument> index_table = new HashMap<>();
    
    /**
     * Default Constructor
     */
    public DocumentCollection() {
        
    }
    
    /**
     * Searches for an Encrypted Document based on a Token
     * 
     * @param token
     * @return 
     */
    public List<EncryptedDocument> Search(Token token) {
        
        List<EncryptedDocument> ret = new ArrayList<>();
        
         for(List<Token> key : index_table.keySet()) {
            // Gets an EncryptedDocument (document) based on the given Token.
            if(key.contains(token)) {
                ret.add(index_table.get(key));
            }
        }
        
        return ret;
    } 
    
    /**
     * Constructs the index_table from a list of database entries
     * 
     * @param entries 
     */
    public void fromDatabase(List<Entry> entries) {
        
        // Load all the entires to the index_table
        for(Entry n : entries) {
            
            // Load the document
            EncryptedDocument document = new EncryptedDocument(n.getID(), n.getDocument(), n.getUsers());
            
            // Construct the list of Tokens
            List<Token> tokens = n.getTokens().stream().map(s -> new Token(s)).collect(Collectors.toList());
            
            // If tokens is empty add the Document ID as a token
            if(tokens.isEmpty()) {
                tokens.add(new Token(n.getID()));
            }
            
            // Add the index to the table
            index_table.put(tokens, document);            
        }
        
    }
    
    /**
     * Constructs 
     * 
     * 
     */
    public List<Entry> toEntries() {
        List<Entry> entries = new ArrayList<>();
        
        for(List<Token> key : index_table.keySet()) {
            // Get the document and Tokens list as strings.
            EncryptedDocument value = index_table.get(key);
            List<String> tokens_mapped = key.stream().map(n -> n.toString()).collect(Collectors.toList());
            
            // Appends the entry to the list of returning entries
            entries.add(new Entry(value.getID(), value.getEncoded_file(), tokens_mapped, value.getUsers()));
        }
        
        return entries;
    }
     
    
    
}
