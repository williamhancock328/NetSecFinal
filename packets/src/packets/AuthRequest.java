package packets;

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
public class AuthRequest implements Packet, JSONSerializable {
        
    // Packet Type
    private static final PacketType PACKET_TYPE = PacketType.AuthRequest;
    
    // Packet Data
    private String user;
    private String pass;
    private int otp;
    private String nonce;

    /**
     * Constructs a new AuthRequest packet
     * @param user
     * @param pass
     * @param opt 
     * @param nonce 
     */
    public AuthRequest(String user, String pass, int opt, String nonce) {
        this.user = user;
        this.pass = pass;
        this.otp = opt;
        this.nonce = nonce;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public int getOtp() {
        return otp;
    }

    public String getNonce() {
        return nonce;
    }

    

    /**
     * Converts a JSONObject into a ticket object
     * @param packet byte[] of information representing this packet
     * @throws InvalidObjectException Thrown if {@code object} is not a Ticket JSONObject
     */
    public AuthRequest(String packet, PacketType packetType1) throws InvalidObjectException {
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

        if (obj instanceof JSONObject) {
            tmp = (JSONObject) obj;
            if (tmp.containsKey("user")) {
                this.user = tmp.getString("user");
            } else {
                throw new InvalidObjectException("Expected an Ticket object -- user expected.");
            }
            if (tmp.containsKey("pass")) {
                this.pass = tmp.getString("pass");
            } else {
                throw new InvalidObjectException("Expected an Ticket object -- pass expected.");
            }
            if (tmp.containsKey("otp")) {
                this.otp = tmp.getInt("otp");
            } else {
                throw new InvalidObjectException("Expected an Ticket object -- otp expected.");
            }
            if (tmp.containsKey("nonce")) {
                this.nonce = tmp.getString("nonce");
            } else {
                throw new InvalidObjectException("Expected an Ticket object -- nonce expected.");
            }
        } else {
            throw new InvalidObjectException("Expected a Ticket - Type JSONObject not found.");
        }
    }

    /**
     * Constructs a JSON object representing this Ticket.
     * @return 
     */
    @Override
    public JSONType toJSONType() {
        JSONObject object = new JSONObject();
        object.put("packetType", PACKET_TYPE.toString());
        object.put("user", this.user);
        object.put("pass", this.pass);
        object.put("otp", this.otp);
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
            Logger.getLogger(AuthRequest.class.getName()).log(Level.SEVERE, null, ex);
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
