package packets;

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
public class SessionKeyResponse implements Packet, JSONSerializable {
    
    // Packet Type
    private static final PacketType PACKET_TYPE = PacketType.SessionKeyResponse;    
    
    // Packet Data
    private String uIv;
    private String eSKeyAlice;
    private long createTime;
    private long validityTime;
    private String uName;
    private String sName;
    private String iv;
    private String eSKey;

    /**
     * Default Constructor for a SessionKeyResponse
     * @param uIv
     * @param eSKeyAlice
     * @param createTime
     * @param validityTime
     * @param uName
     * @param sName
     * @param iv
     * @param eSKey 
     */
    public SessionKeyResponse(String uIv, String eSKeyAlice, long createTime, long validityTime, String uName, String sName, String iv, String eSKey) {
        this.uIv = uIv;
        this.eSKeyAlice = eSKeyAlice;
        this.createTime = createTime;
        this.validityTime = validityTime;
        this.uName = uName;
        this.sName = sName;
        this.iv = iv;
        this.eSKey = eSKey;
    }
    
    /**
     * Overloaded Constructor
     * @param createTime
     * @param validityTime
     * @param uName
     * @param sName 
     */
    public SessionKeyResponse(long createTime, long validityTime, String uName, String sName) {
        this.createTime = createTime;
        this.validityTime = validityTime;
        this.uName = uName;
        this.sName = sName;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public void seteSKey(String eSKey) {
        this.eSKey = eSKey;
    }

    public String geteSKeyAlice() {
        return eSKeyAlice;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getValidityTime() {
        return validityTime;
    }

    public String getIv() {
        return iv;
    }

    public String geteSKey() {
        return eSKey;
    }

    public String getsName() {
        return sName;
    }

    public String getuIv() {
        return uIv;
    }

    public String getuName() {
        return uName;
    }
    
    
    /**
     * Converts a JSONObject into a ticket object
     * @param packet byte[] of information representing this packet
     * @throws InvalidObjectException Thrown if {@code object} is not a Ticket JSONObject
     */
    public SessionKeyResponse(String packet, PacketType packetType) throws InvalidObjectException {
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
            
            if (tmp.containsKey("uIv"))
              this.uIv = tmp.getString("uIv");
            else
              throw new InvalidObjectException("Expected an Ticket object -- uIv expected.");
            
            if (tmp.containsKey("eSKeyAlice"))
              this.eSKeyAlice = tmp.getString("eSKeyAlice");
            else
              throw new InvalidObjectException("Expected an Ticket object -- eSKeyAlice expected.");
            
            if (tmp.containsKey("createTime"))
              this.createTime = Long.parseLong(tmp.getString("createTime"));
            else
              throw new InvalidObjectException("Expected an Ticket object -- createTime (String --> Long) expected.");
            
            if (tmp.containsKey("validityTime"))
              this.validityTime = Long.parseLong(tmp.getString("validityTime"));
            else
              throw new InvalidObjectException("Expected an Ticket object -- validityTime (String --> Long) expected.");
            
            if (tmp.containsKey("uName"))
              this.uName = tmp.getString("uName");
            else
              throw new InvalidObjectException("Expected an Ticket object -- uName expected.");
            
            if (tmp.containsKey("sName"))
              this.sName = tmp.getString("sName");
            else
              throw new InvalidObjectException("Expected an Ticket object -- sName expected.");
 
            if (tmp.containsKey("iv"))
              this.iv = tmp.getString("iv");
            else 
              throw new InvalidObjectException("Expected an Ticket object -- iv (String) expected.");
            
            if (tmp.containsKey("eSKey"))
              this.eSKey = tmp.getString("eSKey");
            else 
              throw new InvalidObjectException("Expected an Ticket object -- eSKey (String) expected.");
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
        
        object.put("packetType", PACKET_TYPE.toString()); // MUST BE PRESENT FOR ALL PACKETS
        object.put("uIv", ""+this.uIv);
        object.put("eSKeyAlice", ""+this.eSKeyAlice);
        object.put("createTime", ""+this.createTime);
        object.put("validityTime", ""+this.validityTime);
        object.put("uName", this.uName);
        object.put("sName", this.sName);
        object.put("iv", this.iv);
        object.put("eSKey", this.eSKey);

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
            Logger.getLogger(SessionKeyResponse.class.getName()).log(Level.SEVERE, null, ex);
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
