package packets.filepack;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;
import packets.Packet;
import packets.PacketType;

/**
 * Represents a Client requesting for a file
 * This is a Client -> Server packet
 * The associating response is FileSearchResponse
 * @author Alex
 */
public class FileSearchRequest implements Packet, JSONSerializable {
        
    // Packet Type
    private static final PacketType PACKET_TYPE = PacketType.FileSearchRequest;
    
    // Packet Data
    private List<String> keywords; // List of the Search Tokens for finding the associating file
    private String username; // Username of the client sending the request.

    /**
     * Constructs a new FileSearchRequest packet
     */
    public FileSearchRequest(List<String> keywords, String username) {
        this.keywords = keywords;
        this.username = username;
    }

    /**
     * Converts a JSONObject into a ticket object
     * @param packet byte[] of information representing this packet
     * @throws InvalidObjectException Thrown if {@code object} is not a Ticket JSONObject
     */
    public FileSearchRequest(String packet, PacketType packetType1) throws InvalidObjectException {
        recieve(packet);
    }

    /**
     * JSONSerializable implementations
     */
    
    /**
     * Serializes the object into a JSON String
     * @return 
     */
    @Override
    public String serialize() {
        return toJSONType().toJSON();
    }

    /**
     * Converts a JSON type to this object
     * Converts types of Byte[] into strings for travel
     * @param jsont
     * @throws InvalidObjectException 
     */
    @Override
    public void deserialize(JSONType obj) throws InvalidObjectException {
        JSONObject tmp;

        if (obj instanceof JSONObject)
          {
            tmp = (JSONObject)obj;
            if (tmp.containsKey("keywords")) {
                this.keywords = new ArrayList<>();
                for(Object object : tmp.getArray("keywords")) {
                    if(object instanceof String) {
                        this.getKeywords().add((String) object);
                    }
                }
            } else {
              throw new InvalidObjectException("Expected an FileSearchRequest object -- keywords array expected.");
            }
            if (tmp.containsKey("username")) {
              this.username = tmp.getString("username");
            } else {
              throw new InvalidObjectException("Expected an FileSearchRequest object -- username expected.");
            }
          }
          else 
            throw new InvalidObjectException("Expected a FileSearchRequest - Type JSONObject not found.");
    }

    /**
     * Constructs a JSON object representing this Ticket.
     * @return 
     */
    @Override
    public JSONType toJSONType() {
        JSONObject object = new JSONObject();
        object.put("packetType", PACKET_TYPE.toString());
        
        JSONArray json_keywords = new JSONArray();
        json_keywords.addAll(this.getKeywords());
       
        object.put("keywords", json_keywords);
        object.put("username", this.getUsername());

        return object;
    }

    /**
     * Packet implementations.
     */
    
    /**
     * Constructs a packet based off of this object
     * A packet is a byte[], we are using JSON to store the packet data.
     * @return A byte[] of packet information
     */    
    @Override
    public String send() {
        
        String jsonString = serialize(); // Convert to String from this class.
        return jsonString; // Convert JSON string to byte[]
    }

    /**
     * Constructs an object T based off of the given packet
     * Generally, this should be used when reading packets in.
     * @param packet input byte[]
     */
    @Override
    public void recieve(String packet) {
        
        try {
             JSONObject jsonObject = JsonIO.readObject(packet); // String to JSONObject
             deserialize(jsonObject); // Deserialize jsonObject
        } catch (InvalidObjectException ex) {
            Logger.getLogger(FileSearchRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * The PacketType value of this packet.
     * @return 
     */
    @Override
    public PacketType getType() {
        return PACKET_TYPE;
    }

    /**
     * @return the keywords
     */
    public List<String> getKeywords() {
        return keywords;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }
    
}