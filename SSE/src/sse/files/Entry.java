package sse.files;

import java.io.InvalidObjectException;
import java.util.List;
import merrimackutil.json.JSONSerializable;
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
    public Entry(String ID, String document, List<String> tokens, List<String> users) {
        this.ID = ID;
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
            
            if(obj.containsKey("ID")) {
                this.ID = obj.getString("ID");
            } else { throw new InvalidObjectException("Expected an Host object -- host-name expected."); }
                        
            if(obj.containsKey("address")) {
                this.address = obj.getString("address");
            } else { throw new InvalidObjectException("Expected an Host object -- address expected."); }
            
            if(obj.containsKey("port")) {
                this.port = obj.getInt("port");
            } else { throw new InvalidObjectException("Expected an Host object -- port expected."); }
        }
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("host-name", this.getHost_name());
        obj.put("address", this.getAddress());
        obj.put("port", this.getPort());

        return obj; // We should never be writing to a file.
    }
    
    
    /**
     * Accessors
     */
    
    /**
     * @return the host_name
     */
    public String getHost_name() {
        return host_name;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }
    
    
}
