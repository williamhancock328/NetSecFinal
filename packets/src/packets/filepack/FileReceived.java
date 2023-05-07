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
 * Represents a piece of a file being sent
 * Middle part to the file sending protocol.
 * @author Alex
 */
public class FileReceived implements Packet, JSONSerializable {
        
    // Packet Type
    private static final PacketType PACKET_TYPE = PacketType.FileReceived;
    
    // Packet Data
    private boolean received; // Boolean if the file_bit was reveived with no issues
    private String fileID = ""; // May be null if the user is sending this too the server.

    /**
     * Constructs a new AuthRequest packet
     * @param user
     * @param pass
     * @param opt 
     */
    public FileReceived(boolean received, String fileID) {
        this.received = received;
        this.fileID = fileID;
    }

    /**
     * Converts a JSONObject into a ticket object
     * @param packet byte[] of information representing this packet
     * @throws InvalidObjectException Thrown if {@code object} is not a Ticket JSONObject
     */
    public FileReceived(String packet, PacketType packetType1) throws InvalidObjectException {
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
            if (tmp.containsKey("received"))
              this.received = tmp.getBoolean("received");
            else
              throw new InvalidObjectException("Expected an FileReceived object -- received expected.");
            if (tmp.containsKey("fileID"))
              this.fileID = tmp.getString("fileID");
            else
              throw new InvalidObjectException("Expected an FileReceived object -- fileID expected.");
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
        object.put("received", this.isReceived());
        object.put("fileID", this.getFileID()); // may be null / empty

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
            Logger.getLogger(FileReceived.class.getName()).log(Level.SEVERE, null, ex);
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
     * @return the received
     */
    public boolean isReceived() {
        return received;
    }

    /**
     * @return the fileID
     */
    public String getFileID() {
        return fileID;
    }
    
}