package packets.filepack;

import java.io.InvalidObjectException;
import java.util.logging.Level;
import java.util.logging.Logger;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;
import packets.Packet;
import packets.PacketType;

/**
 * Represents a Server responding to a clients search request for a file.* This is a Client -> Server packet
 * This is a Server --> Client packet.
 * The associating response is FileSearchRequest
 * @author Alex
 */
public class FileSearchResponse implements Packet, JSONSerializable {
        
    // Packet Type
    private static final PacketType PACKET_TYPE = PacketType.FileSearchResponse;
    
    // Packet Data
    private boolean accessed; // Boolean if the file_bit was reveived with no issues
    private String ID = ""; // File ID of the assocaiting searched file
    private String encrypted_filename; // Encrypted filename of the file. This should always be present.
    
    /**
     * Constructs a new FileSearchRequest packet
     */
    public FileSearchResponse(boolean accessed, String ID, String encrypted_filename) {
        this.accessed = accessed;
        this.ID = ID;
        this.encrypted_filename = encrypted_filename;
    }

    /**
     * Converts a JSONObject into a ticket object
     * @param packet byte[] of information representing this packet
     * @throws InvalidObjectException Thrown if {@code object} is not a Ticket JSONObject
     */
    public FileSearchResponse(String packet, PacketType packetType1) throws InvalidObjectException {
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
            if (tmp.containsKey("accessed"))
              this.accessed = tmp.getBoolean("accessed");
            else
              throw new InvalidObjectException("Expected an FileSearchResponse object -- accessed expected.");
            if (tmp.containsKey("ID"))
              this.ID = tmp.getString("ID");
            else
              throw new InvalidObjectException("Expected an FileSearchResponse object -- ID expected.");
            if (tmp.containsKey("encrypted_filename"))
              this.encrypted_filename = tmp.getString("encrypted_filename");
            else
              throw new InvalidObjectException("Expected an FileSearchResponse object -- encrypted_filename expected.");
          }
          else 
            throw new InvalidObjectException("Expected a FileSearchResponse - Type JSONObject not found.");
    }

    /**
     * Constructs a JSON object representing this Ticket.
     * @return 
     */
    @Override
    public JSONType toJSONType() {
        JSONObject object = new JSONObject();
        object.put("packetType", PACKET_TYPE.toString());
        
        object.put("accessed", this.isAccessed());
        object.put("ID", this.getID());
        object.put("encrypted_filename", this.getEncrypted_filename());

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
            Logger.getLogger(FileSearchResponse.class.getName()).log(Level.SEVERE, null, ex);
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
     * @return the accessed
     */
    public boolean isAccessed() {
        return accessed;
    }

    /**
     * @return the ID
     */
    public String getID() {
        return ID;
    }

    /**
     * @return the encrypted_filename
     */
    public String getEncrypted_filename() {
        return encrypted_filename;
    }

}