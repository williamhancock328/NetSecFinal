package sse.files;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Represents an entry in the cloud database.
 * An entry consist of
 *   - ID for the document, representing the Primary Key.
 *   - A document.
 *   - Tokens for the document.
 *   - Users who have access to that document.
 * @author Alex Elguezabal
 */
public class Entry implements JSONSerializable {
        
    // Host Data
    private String ID; // (Primary Key) Represents the ID of the document in the database 
    private String encrypted_filename; // File name encrypted with the File-Password key
    private String document; // Represents the encrypted document
    private List<String> tokens; // Represents the tokens for {@code document}
    private List<String> users; // Represents the users who have access to {@code document}
    
    /**
     * Constructor for loading.
     * @param obj
     * @throws InvalidObjectException 
     */
    public Entry(JSONObject obj) throws InvalidObjectException {
        deserialize(obj); // Deserialize a host into this host object
    }         
    
    /**
     * Constructor for creating a new entry.
     * @param ID
     * @param document
     * @param tokens
     * @param users 
     */
    public Entry(String ID, String encrypted_filename, String document, List<String> tokens, List<String> users) {
        this.ID = ID;
        this.encrypted_filename = encrypted_filename;
        this.document = document;
        this.tokens = tokens;
        this.users = users;
    }

    @Override
    public String serialize() {
        return toJSONType().getFormattedJSON(); // Should never be called
    }

    @Override
    public void deserialize(JSONType jsont) throws InvalidObjectException {
        if(jsont instanceof JSONObject) {
            JSONObject obj = (JSONObject) jsont;
            
            // ID 
            if(obj.containsKey("ID")) {
                this.ID = obj.getString("ID");
            } else { throw new InvalidObjectException("Expected an Entry object -- ID expected."); }
                  
            // Filename
            if(obj.containsKey("encrypted_filename")) {
                this.encrypted_filename = obj.getString("encrypted_filename");
            } else { throw new InvalidObjectException("Expected an Entry object -- encrypted_filename expected."); }
                    
            // Document
            if(obj.containsKey("document")) {
                this.document = obj.getString("document");
            } else { throw new InvalidObjectException("Expected an Entry object -- document expected."); }
            
            // Tokens
            if(obj.containsKey("tokens")) {
                this.tokens = new ArrayList<>();
                for(Object o : obj.getArray("tokens")) {
                    if(o instanceof String) {
                        this.tokens.add((String)o);
                    }
                }
            } else { throw new InvalidObjectException("Expected an Entry object -- array tokens expected."); }
            
            // Users
            if(obj.containsKey("users")) {
                this.users = new ArrayList<>();
                for(Object o : obj.getArray("users")) {
                    if(o instanceof String) {
                        this.users.add((String)o);
                    }
                }
            } else { throw new InvalidObjectException("Expected an Entry object -- array users expected."); }
        }
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        
        JSONArray json_tokens = new JSONArray();
        json_tokens.addAll(getTokens());
        
        JSONArray json_users = new JSONArray();
        json_users.addAll(getUsers());
        
        obj.put("ID", this.getID());
        obj.put("encrypted_filename", this.getEncrypted_filename());
        obj.put("document", this.getDocument());
        obj.put("tokens", json_tokens);
        obj.put("users", json_users);

        return obj; // We should never be writing to a file.
    }

    /**
     * Accessors
     */
    
    /**
     * @return the ID
     */
    public String getID() {
        return ID;
    }

    /**
     * @return the document
     */
    public String getDocument() {
        return document;
    }

    /**
     * @return the tokens
     */
    public List<String> getTokens() {
        return tokens;
    }

    /**
     * @return the users
     */
    public List<String> getUsers() {
        return users;
    } 

    /**
     * @return the encrypted_filename
     */
    public String getEncrypted_filename() {
        return encrypted_filename;
    }
   
}
