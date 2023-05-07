package packets.abstractpk;

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
 *
 * @author Alex
 */
public class SessionKeyPackets implements JSONSerializable, Packet {

      // Packet Type
    private static final PacketType PACKET_TYPE = PacketType.SessionKeyPackets;
    
    
    private String iv;
    private String nonce;
    private String encrypted_packet;

    public SessionKeyPackets(String iv, String nonce, String encrypted_packet) {
        this.iv = iv;
        this.nonce = nonce;
        this.encrypted_packet = encrypted_packet;
    }

    /**
     * Converts a JSONObject into a ticket object
     *
     * @param packet byte[] of information representing this packet
     * @throws InvalidObjectException Thrown if {@code object} is not a Ticket
     * JSONObject
     */
    public SessionKeyPackets(String packet, PacketType packetType1) throws InvalidObjectException {
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
            if (tmp.containsKey("iv")) {
                this.iv = tmp.getString("iv");
            } else {
                throw new InvalidObjectException("Expected an FileCreate object -- iv expected.");
            }
            if (tmp.containsKey("nonce")) {
                this.nonce = tmp.getString("nonce");
            } else {
                throw new InvalidObjectException("Expected an FileCreate object -- nonce expected.");
            }
            if (tmp.containsKey("encrypted_packet")) {
                this.encrypted_packet = tmp.getString("encrypted_packet");
            } else {
                throw new InvalidObjectException("Expected an FileCreate object -- encrypted_packet expected.");
            }
        } else {
            throw new InvalidObjectException("Expected a FileCreate - Type JSONObject not found.");
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

        object.put("iv", this.iv);
        object.put("nonce", this.nonce);
        object.put("encrypted_packet", this.encrypted_packet);

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
            Logger.getLogger(SessionKeyPackets.class.getName()).log(Level.SEVERE, null, ex);
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

    public String getIv() {
        return iv;
    }

    public String getNonce() {
        return nonce;
    }

    public String getEncrypted_packet() {
        return encrypted_packet;
    }


}
