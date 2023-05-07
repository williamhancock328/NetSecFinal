package packets.abstractpk;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;
import packets.Packet;

/**
 *
 * @author Alex
 */
public class SessionKeyPackets implements JSONSerializable, Packet {
    
    private String IV;
    private String nonce;
    private String encrypted_packet;
    
    public SessionKeyPackets(String IV, String nonce, String encrypted_packet) {
        this.IV = IV;
        this.nonce = nonce;
        this.encrypted_packet = encrypted_packet;
    }
    

}
