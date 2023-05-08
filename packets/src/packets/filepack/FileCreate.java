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
 * Represents a file Create Packet
 * This is a Client --> Server packet
 * @author Alex
 */
public class FileCreate implements Packet, JSONSerializable {
        
    // Packet Type
    private static final PacketType PACKET_TYPE = PacketType.FileCreate;
    
    // Packet Data
    private String encrypted_filename;
    private List<String> users;
    private List<String> tokens;

    /**
     * Constructs a new FileCreate packet
     */
    public FileCreate(String encrypted_filename, List<String> users, List<String> tokens) {
        this.encrypted_filename = encrypted_filename;
        this.users = users;
        this.tokens = tokens;
    }

    /**
     * Converts a JSONObject into a ticket object
     * @param packet byte[] of information representing this packet
     * @throws InvalidObjectException Thrown if {@code object} is not a Ticket JSONObject
     */
    public FileCreate(String packet, PacketType packetType1) throws InvalidObjectException {
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
            if (tmp.containsKey("encrypted_filename"))
              this.encrypted_filename = tmp.getString("encrypted_filename");
            else
              throw new InvalidObjectException("Expected an FileCreate object -- encrypted_filename expected.");
            if (tmp.containsKey("users")) {
                this.users = new ArrayList<>();
                for(Object object : tmp.getArray("users")) {
                    if(object instanceof String) {
                        this.getUsers().add((String) object);
                    }
                }
            } else {
              throw new InvalidObjectException("Expected an FileCreate object -- users array expected.");
            }
            if (tmp.containsKey("tokens")) {
                this.tokens = new ArrayList<>();
                  for(Object object : tmp.getArray("tokens")) {
                      if(object instanceof String) {
                          this.getTokens().add((String) object);
                      }
                  }
            } else { 
              throw new InvalidObjectException("Expected an FileCreate object -- tokens array expected.");
            }
          }
          else 
            throw new InvalidObjectException("Expected a FileCreate - Type JSONObject not found.");
    }

    /**
     * Constructs a JSON object representing this Ticket.
     * @return 
     */
    @Override
    public JSONType toJSONType() {
        JSONObject object = new JSONObject();

        JSONArray users_array = new JSONArray();
        users_array.addAll(this.getUsers());
        
        JSONArray tokens_array = new JSONArray();
        tokens_array.addAll(this.getTokens());
        
        object.put("packetType", PACKET_TYPE.toString()); // MUST BE PRESENT FOR ALL PACKETS
        object.put("encrypted_filename", this.getEncrypted_filename());
        object.put("users", users_array);
        object.put("tokens", tokens_array);

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
            Logger.getLogger(FileCreate.class.getName()).log(Level.SEVERE, null, ex);
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
     * @return the users
     */
    public List<String> getUsers() {
        return users;
    }

    /**
     * @return the tokens
     */
    public List<String> getTokens() {
        return tokens;
    }

    /**
     * @return the encrypted_filename
     */
    public String getEncrypted_filename() {
        return encrypted_filename;
    }
}