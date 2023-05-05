package packets;

import java.util.Arrays;

/**
 * Represents the type of packet being sent/received
 * Uses for determining responses & information.
 * @author Alex
 */
public enum PacketType {
    
      EnrollRequest(packets.EnrollRequest.class),
      AuthRequest(packets.AuthRequest.class),
      ServerResponse(packets.ServerResponse.class),
      FileUpload(packets.filepack.FileUpload.class),
      SessionKeyRequest(packets.SessionKeyRequest.class),
      SessionKeyResponse(packets.SessionKeyResponse.class),
      ClientHello(packets.ClientHello.class),
      ClientResponse(packets.ClientResponse.class),
      CommPhase(packets.CommPhase.class),
      ServerHello(packets.ServerHello.class),
      Ticket(packets.Ticket.class),
      HandshakeStatus(packets.HandshakeStatus.class)
      ;

      
    private Class packetClass;
    
    PacketType(Class packetClass) {
        this.packetClass = packetClass;
    }
    
    /**
     * The associated class representing a PacketType
     * @return 
     */
    public Class getPacketClass() {
        return this.packetClass;
    }
    
    /**
     * Gets a packet from a String {@code name}
     * @param name
     * @return 
     */
    public static PacketType getPacketTypeFromString(String name) {
        return Arrays.stream(values())
                .filter(n -> n.toString().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
    
}
