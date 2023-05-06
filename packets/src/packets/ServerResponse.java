package packets;

import packets.PacketType;
import packets.Packet;
import java.io.InvalidObjectException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 *
 * @author William Hancock
 */
public class ServerResponse implements Packet, JSONSerializable {
        
    // Packet Type
    private static final PacketType PACKET_TYPE = PacketType.ServerResponse;
    
    // Packet Data
    private boolean status;
    private String payload;
    private String nonce;

    /**
     * Constructs a new ServerResponse packet
     * @param user
     * @param pass
     * @param opt 
     */
    public ServerResponse(boolean status, String payload,String nonce) {
        this.status = status;
        this.payload = payload;
        this.nonce = nonce;
    }

    /**
     * Converts a JSONObject into a ticket object
     * @param packet byte[] of information representing this packet
     * @throws InvalidObjectException Thrown if {@code object} is not a Ticket JSONObject
     */
    public ServerResponse(String packet, PacketType packetType1) throws InvalidObjectException {
        recieve(packet);
    }

    public boolean getStatus() {
        return status;
    }

    public String getPayload() {
        return payload;
    }

    public String getNonce() {
        return nonce;
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
            if (tmp.containsKey("status"))
              this.status = tmp.getBoolean("status");
            else
              throw new InvalidObjectException("Expected an Ticket object -- status expected.");
            if (tmp.containsKey("payload"))
              this.payload = tmp.getString("payload");
            else
              throw new InvalidObjectException("Expected an Ticket object -- payload expected.");
             if (tmp.containsKey("nonce"))
              this.nonce = tmp.getString("nonce");
            else
              throw new InvalidObjectException("Expected an Ticket object -- nonce expected.");
          }
          else 
            throw new InvalidObjectException("Expected a Ticket - Type JSONObject not found.");
    }

    /**
     * Constructs a JSON object representing this Ticket.
     * @return 
     */
    @Override
    public JSONType toJSONType() {
        JSONObject object = new JSONObject();
        object.put("packetType", PACKET_TYPE.toString());
        object.put("status", this.status);
        object.put("payload", this.payload);
        object.put("nonce", this.nonce);
        

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
            Logger.getLogger(ServerResponse.class.getName()).log(Level.SEVERE, null, ex);
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
    
}
