package packets;

import java.io.InvalidObjectException;
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
public class CommPhase implements Packet, JSONSerializable {

    // Packet Type
    private static final PacketType PACKET_TYPE = PacketType.CommPhase;

    // Packet Data
    private String nonce;
    private String cName;
    private String sName;
    private String iv;
    private String eMsg;

    /**
     * Default Constructor for a SessionKeyResponse
     *
     * @param nonce
     * @param cName
     * @param sName
     * @param iv
     * @param eMsg
     */
    public CommPhase(String nonce, String cName, String sName, String iv, String eMsg) {
        this.nonce = nonce;
        this.cName = cName;
        this.sName = sName;
        this.iv = iv;
        this.eMsg = eMsg;
    }

    public static PacketType getPACKET_TYPE() {
        return PACKET_TYPE;
    }

    public String getNonce() {
        return nonce;
    }

    public String getcName() {
        return cName;
    }

    public String getsName() {
        return sName;
    }

    public String getIv() {
        return iv;
    }

    public String geteMsg() {
        return eMsg;
    }

    
    
    /**
     * Converts a JSONObject into a ticket object
     *
     * @param packet byte[] of information representing this packet
     * @throws InvalidObjectException Thrown if {@code object} is not a Ticket
     * JSONObject
     */
    public CommPhase(String packet, PacketType packetType) throws InvalidObjectException {
        recieve(packet);
    }

    /**
     * JSONSerializable implementations
     */
    /**
     * Serializes the object into a JSON String
     *
     * @return
     */
    @Override
    public String serialize() {
        return toJSONType().toJSON();
    }

    /**
     * Converts a JSON type to this object Converts types of Byte[] into strings
     * for travel
     *
     * @param jsont
     * @throws InvalidObjectException
     */
    @Override
    public void deserialize(JSONType obj) throws InvalidObjectException {
        JSONObject tmp;

        if (obj instanceof JSONObject) {
            tmp = (JSONObject) obj;

            if (tmp.containsKey("nonce")) {
                this.nonce = tmp.getString("nonce");
            } else {
                throw new InvalidObjectException("Expected an Ticket object -- nonce expected.");
            }

            if (tmp.containsKey("cName")) {
                this.cName = tmp.getString("cName");
            } else {
                throw new InvalidObjectException("Expected an Ticket object -- cName expected.");
            }
            
            if (tmp.containsKey("sName")) {
                this.sName = tmp.getString("sName");
            } else {
                throw new InvalidObjectException("Expected an Ticket object -- sName expected.");
            }

            if (tmp.containsKey("iv")) {
                this.iv = tmp.getString("iv");
            } else {
                throw new InvalidObjectException("Expected an Ticket object -- iv (String) expected.");
            }

            if (tmp.containsKey("eMsg")) {
                this.eMsg = tmp.getString("eMsg");
            } else {
                throw new InvalidObjectException("Expected an Ticket object -- eMsg (String) expected.");
            }
        } else {
            throw new InvalidObjectException("Expected a Ticket - Type JSONObject not found.");
        }
    }

    /**
     * Constructs a JSON object representing this Ticket.
     *
     * @return
     */
    @Override
    public JSONType toJSONType() {
        JSONObject object = new JSONObject();

        object.put("packetType", PACKET_TYPE.toString()); // MUST BE PRESENT FOR ALL PACKETS
        object.put("nonce", this.nonce);
        object.put("cName", this.cName);
        object.put("sName", this.sName);
        object.put("iv", this.iv);
        object.put("eMsg", this.eMsg);

        return object;
    }

    /**
     * Packet implementations.
     */
    /**
     * Constructs a packet based off of this object A packet is a byte[], we are
     * using JSON to store the packet data.
     *
     * @return A byte[] of packet information
     */
    @Override
    public String send() {

        String jsonString = serialize(); // Convert to String from this class.
        return jsonString; // Convert JSON string to byte[]
    }

    /**
     * Constructs an object T based off of the given packet Generally, this
     * should be used when reading packets in.
     *
     * @param packet input byte[]
     */
    @Override
    public void recieve(String packet) {

        try {
            JSONObject jsonObject = JsonIO.readObject(packet); // String to JSONObject
            deserialize(jsonObject); // Deserialize jsonObject
        } catch (InvalidObjectException ex) {
            Logger.getLogger(SessionKeyResponse.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * The PacketType value of this packet.
     *
     * @return
     */
    @Override
    public PacketType getType() {
        return PACKET_TYPE;
    }
}
