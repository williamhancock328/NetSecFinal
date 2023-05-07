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
 * This packet can used by either the client or the server.
 * @author Alex
 */
public class FileSend implements Packet, JSONSerializable, Comparable<FileSend> {
        
    // Packet Type
    private static final PacketType PACKET_TYPE = PacketType.FileSend;
    
    // Packet Data
    private String ID; // ID of the file, this is necisary.
    private String file_bit;
    private int index;
    private boolean isfinal;

    /**
     * Constructs a new FileSend packet
     */
    public FileSend(String ID, String file_bit, int index, boolean isfinal) {
        this.ID = ID;
        this.file_bit = file_bit;
        this.index = index;
        this.isfinal = isfinal;
    }

    /**
     * Converts a JSONObject into a ticket object
     * @param packet byte[] of information representing this packet
     * @throws InvalidObjectException Thrown if {@code object} is not a Ticket JSONObject
     */
    public FileSend(String packet, PacketType packetType1) throws InvalidObjectException {
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
              this.file_bit = tmp.getString("ID");
            else
              throw new InvalidObjectException("Expected an FileSend object -- ID expected.");
            if (tmp.containsKey("file_bit"))
              this.file_bit = tmp.getString("file_bit");
            else
              throw new InvalidObjectException("Expected an FileSend object -- file_bit expected.");
            if (tmp.containsKey("index"))
              this.index = tmp.getInt("index");
            else
              throw new InvalidObjectException("Expected an FileSend object -- index expected.");
            if (tmp.containsKey("isfinal"))
                this.setIsfinal((boolean) tmp.getBoolean("isfinal"));
            else
              throw new InvalidObjectException("Expected an FileSend object -- isfinal expected.");
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
        object.put("file_bit", this.getFile_bit());
        object.put("index", this.getIndex());
        object.put("isfinal", this.isIsfinal());

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
            Logger.getLogger(FileSend.class.getName()).log(Level.SEVERE, null, ex);
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
     * @return the file_bit
     */
    public String getFile_bit() {
        return file_bit;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the isfinal
     */
    public boolean isIsfinal() {
        return isfinal;
    }
    
    /**
     * @param isfinal the isfinal to set
     */
    public void setIsfinal(boolean isfinal) {
        this.isfinal = isfinal;
    }

    /**
     * CompareTo for FileSend object.
     * Compares if the index is greater than or not.
     * @param o
     * @return 
     */
    @Override
    public int compareTo(FileSend o) {
        return this.getIndex() == o.getIndex() ? 0 : (this.getIndex() > o.getIndex() ? 1 : -1 );
    }

    /**
     * @return the ID
     */
    public String getID() {
        return ID;
    }
    
}