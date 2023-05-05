package communication;

import packets.ServerResponse;
import packets.PacketType;
import packets.Packet;
import packets.EnrollRequest;
import packets.AuthRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import static packets.PacketType.AuthRequest;
import static packets.PacketType.EnrollRequest;
import static packets.PacketType.ServerResponse;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import packets.ClientHello;
import packets.ClientResponse;
import packets.CommPhase;
import packets.HandshakeStatus;
import packets.ServerHello;
import packets.SessionKeyRequest;
import packets.SessionKeyResponse;
import packets.Ticket;

/**
 * Utility class used for sending packets across servers
 *
 * @author Alex Elguezabal, modified by William Hancock
 */
public class Communication {

    /**
     * Used to send a message to a socket {@code peer}
     *
     * @param peer
     * @param message
     * @throws IOException
     */
    public static void send(Socket peer, Packet message) throws IOException {
        new PrintWriter(peer.getOutputStream(), true).println(message.send());
        peer.getOutputStream().flush(); // Flush after each message
    }

    /**
     * Connects to a socket and sends a packet Used for client-side messaging
     * where we are sending single message instances.
     *
     * @param address Address of the server
     * @param port Port to connect too on the server
     * @param messgae Packet to be sent
     */
    public static SSLSocket connectAndSend(String address, int port, Packet messgae) throws IOException {
        SSLSocketFactory fac;
        SSLSocket sock = null;
        try {
            // Set up a connection to the echo server running on the same machine.
            // Set up the trust store.
            System.setProperty("javax.net.ssl.trustStore", "truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "test12345");

            // Set up a connection to the echo server running on the same machine
            // using SSL.
            fac = (SSLSocketFactory) SSLSocketFactory.getDefault();

            sock = (SSLSocket) fac.createSocket("localhost", port);

            sock.startHandshake();

        } catch (UnknownHostException ex) {
            System.out.println("Host [" + address + " " + port + "] connected could not be established.");
        } catch (IOException ioe) {
            System.out.println("Host [" + address + " " + port + "] connected could not be established.");
            ioe.printStackTrace();
        }

        send(sock, messgae);
        return sock;
    }

    /**
     * Used to reed a message from a socket {@code peer} Must update the switch
     * statement with all packet types. **Note: This method is blocking, it will
     * wait for a response before allowing the thread too continue.
     *
     * @param peer
     * @return
     * @throws IOException
     * @throws NoSuchMethodException
     */
    public static Packet read(Socket peer) throws IOException, NoSuchMethodException {
        BufferedReader br = new BufferedReader(new InputStreamReader(peer.getInputStream()));

        // Input from peer
        String line = br.readLine(); // Blocking call, will not run anything until this finished on thread.

        // Determine the type of packet.
        JSONObject object = JsonIO.readObject(line); // All packets are type JSON object with identifier "packetType"
        String identifier = object.getString("packetType");
        PacketType packetType = PacketType.getPacketTypeFromString(identifier);

        // Assure we have the packet type
        if (packetType == null) {
            throw new NullPointerException("No packet called [" + identifier + "] found.");
        }

        // Switch over all of the packet types
        // Using a switch statement to avoid reflection
        switch (packetType) {
            case AuthRequest:
                return new AuthRequest(line, packetType);
            case EnrollRequest:
                return new EnrollRequest(line, packetType);
            case ServerResponse:
                return new ServerResponse(line, packetType);
            case SessionKeyRequest: return new SessionKeyRequest(line, packetType);
            case SessionKeyResponse: return new SessionKeyResponse(line, packetType);
            case ClientHello: return new ClientHello(line, packetType); 
            case ClientResponse: return new ClientResponse(line, packetType); 
            case CommPhase: return new CommPhase(line, packetType); 
            case ServerHello: return new ServerHello(line, packetType); 
            case HandshakeStatus: return new HandshakeStatus(line, packetType);
            case Ticket: return new Ticket(line, packetType);
            default:
                return null;
        }

    }

}
