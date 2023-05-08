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
 * Represents a request for a fragment from the client to the server
 * Middle part to the file sending protocol.
 * Should be used from client --> to request a fragment.
 * @author Alex
 */
public class FileSearchSendRequest implements Packet, JSONSerializable {
        
    // Packet Type
    private static final PacketType PACKET_TYPE = PacketType.FileSend;
    
    // Packet Data
    private String ID; // ID of the file, this is necissary.
    private int last_index; // Index of the last fragment sent

    /**
     * Constructs a new FileSend packet
     */
    public FileSearchSendRequest(String ID, int last_index) {
        this.ID = ID;
        this.last_index = last_index;
    }

    /**
     * Converts a JSONObject into a ticket object
     * @param packet byte[] of information representing this packet
     * @throws InvalidObjectException Thrown if {@code object} is not a Ticket JSONObject
     */
    public FileSearchSendRequest(String packet, PacketType packetType1) throws InvalidObjectException {
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
            if (tmp.containsKey("ID"))
              this.ID = tmp.getString("ID");
            else
              throw new InvalidObjectException("Expected an FileSend object -- ID expected.");
            if (tmp.containsKey("last_index"))
              this.last_index = tmp.getInt("last_index");
            else
              throw new InvalidObjectException("Expected an FileSend object -- file_bit expected.");
          }
          else 
            throw new InvalidObjectException("Expected a FileSend - Type JSONObject not found.");
    }

    /**
     * Constructs a JSON object representing this Ticket.
     * @return 
     */
    @Override
    public JSONType toJSONType() {
        JSONObject object = new JSONObject();
        object.put("packetType", PACKET_TYPE.toString());
        object.put("ID", this.getID());
        object.put("last_index", this.getLast_index());


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
            Logger.getLogger(FileSearchSendRequest.class.getName()).log(Level.SEVERE, null, ex);
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
     * @return the ID
     */
    public String getID() {
        return ID;
    }

    /**
     * @return the last_index
     */
    public int getLast_index() {
        return last_index;
    }
    
}