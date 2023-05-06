package twoauth;

import ClientServerCrypto.ClientMasterKeyDecryption;
import ClientServerCrypto.ClientSessionKeyDecryption;
import ClientServerCrypto.ClientSessionKeyEncryption;
import packets.*;
import java.util.Objects;
import javax.net.ssl.SSLSocket;
import merrimackutil.cli.LongOption;
import merrimackutil.cli.OptionParser;
import merrimackutil.util.Tuple;
import communication.*;
import conf.Config;
import conf.Host;
import java.io.Console;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import merrimackutil.util.NonceCache;

/**
 *
 * @author willi
 */
public class Client {

    public static ArrayList<Host> hosts = new ArrayList<>();
    private static Config config;
    private static String host = "hosts.json";
    private static String port;
    private static String pass;
    private static String user;
    Console console = System.console();
    private static NonceCache nc = new NonceCache(32, 30);
    private static byte[] sessionKeyClient;
    private static String service = "cloudservice";


    /**
     * The client for the file sharing system.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, NoSuchMethodException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        //print a welcome message then a menu with the options to create a user, download a file, upload a file, manage tags, and search for files by tag
        System.out.println("Welcome to the File Sharing System!");
        Console console = System.console();
        Scanner scanner = new Scanner(System.in);
        config = new Config(host);
        System.out.println("Login Menu:");
        System.out.println("1. Create a new user");
        System.out.println("2. Login as an existing user");
        System.out.println("3. Exit");
        Host hosts = getHost("kdcd");
        Scanner scanner2 = new Scanner(System.in);
        int input = scanner2.nextInt();
        switch(input){
            case 1:
                System.out.println("Please enter the username you would like to use: ");
                String newuser = scanner.nextLine();
                String newpass = new String(console.readPassword("Enter password:"));
                create(host, newpass, hosts.getPort(), newuser);
                break;
            case 2:
                System.out.println("Please enter your username: ");
                user = scanner.nextLine();
                pass = new String(console.readPassword("Enter password:"));
                System.out.println("Please enter your one time password: ");
                int otp = scanner.nextInt();
                if(auth(user, pass, hosts.getPort(), user, otp)){
                    System.out.println("");
                    Ticket tik = SessionKeyRequest();
                    System.out.println("Now we have the ticket.");
                    Handshake(tik);
                }
                else{
                    System.out.println("Login failed.");
                }
                break;
            case 3:
                System.exit(0);
                break;
            default:
                System.out.println("Invalid input.");
                break;
        }
    }

    /*
     * Creates a new user.
     * @param host The host to connect to.
     * @param pass The password of the user.
     * @param port The port to connect to.
     * @param user The username of the user.
     * @throws IOException
     */
    private static void create(String host, String pass, int port, String user) throws IOException, NoSuchMethodException {
        EnrollRequest send = new EnrollRequest(user, pass);
        SSLSocket out = Communication.connectAndSend(host, port, send);
        final Packet packet = Communication.read(out);
        ServerResponse ServerResponse_packet = (ServerResponse) packet;
        if (ServerResponse_packet.getStatus()) {
            System.out.println("Base 32 Key: " + ServerResponse_packet.getPayload());
        } else {
            System.out.println(ServerResponse_packet.getPayload());
        }
    }

    /*
     * Authenticates a user.
     * @param host The host to connect to.
     * @param pass The password of the user.
     * @param port The port to connect to.
     * @param user The username of the user.
     * @param otp The one time password of the user.
     * @throws IOException
     */
    private static boolean auth(String host, String pass, int port, String user, int otp) throws IOException, NoSuchMethodException {
        AuthRequest send = new AuthRequest(user, pass, otp);
        SSLSocket out = Communication.connectAndSend(host, port, send);
        final Packet packet = Communication.read(out);
        ServerResponse ServerResponse_packet = (ServerResponse) packet;
        if (ServerResponse_packet.getStatus()) {
            System.out.println("Authenticated.");
            return true;
        } else {
            System.out.println(ServerResponse_packet.getPayload());
            return false;
        }
    }

    /**
     * Method used to upload a file
     *
     * @param host
     * @param pass
     * @param port
     * @param user
     * @param ticketString
     * @param filepath
     */
    private static void uploadFile(String host, String pass, String port, String user, String ticketString, String[] tags, String filepath) {

    }

    private static Host getHost(String host_name) {
        return hosts.stream().filter(n -> n.getHost_name().equalsIgnoreCase(host_name)).findFirst().orElse(null);
    }
    
    private static Ticket SessionKeyRequest() throws IOException, NoSuchMethodException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
        Host host = getHost("kdcd");

        // MESSAGE 1: Client sends kdc username and service name
        SessionKeyRequest req = new SessionKeyRequest(user, service); // Construct the packet
        Socket socket = Communication.connectAndSend(host.getAddress(), host.getPort(), req); // Send the packet

        // MESSAGE 2
        SessionKeyResponse sessKeyResp_Packet = (SessionKeyResponse) Communication.read(socket); // Read for a packet  // KDC checks username validity and if valid, demands password and gives a nonce
        System.out.println("IV");
        System.out.println(sessKeyResp_Packet.getuIv());

        System.out.println("Session key");
        System.out.println(sessKeyResp_Packet.geteSKeyAlice());
        System.out.println(sessKeyResp_Packet.getValidityTime());
        System.out.println(sessKeyResp_Packet.getCreateTime());
        System.out.println(sessKeyResp_Packet.getsName());
        //alice's session key

        sessionKeyClient = ClientMasterKeyDecryption.decrypt(sessKeyResp_Packet.geteSKeyAlice(), sessKeyResp_Packet.getuIv(), user, pass, sessKeyResp_Packet.getCreateTime(), sessKeyResp_Packet.getValidityTime(), sessKeyResp_Packet.getsName());
        System.out.println("Client session key: " + Arrays.toString(sessionKeyClient));
        socket.close();
        //send a ticket
        return new Ticket(sessKeyResp_Packet.getCreateTime(), sessKeyResp_Packet.getValidityTime(), sessKeyResp_Packet.getuName(), sessKeyResp_Packet.getsName(), sessKeyResp_Packet.getIv(), sessKeyResp_Packet.geteSKey());
    }

    private static boolean Handshake(Ticket in) throws IOException, NoSuchMethodException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        //Client connects to echoservice
        Host host = getHost("cloudservice");
        System.out.println("GOT TO ECHOSERVICE");
        // Get fresh nonce C
        byte[] nonceCBytes = nc.getNonce();
        // Convert nonceCBytes to Base64 string format
        String nonceC = Base64.getEncoder().encodeToString(nonceCBytes);

        // Serialize ticket that we will send
        String tkt = in.serialize();

        // MESSAGE 1: Client sends echoservice the nonce C and ticket
        ClientHello hi = new ClientHello(nonceC, tkt); // Construct the packet
        System.out.println(host.getAddress() + host.getPort());
        Socket socket = Communication.connectAndSend(host.getAddress(), host.getPort(), hi); // Send the packet

        //MESSAGE 3: Recieved the server hello
        ServerHello ServerHello_Packet = (ServerHello) Communication.read(socket);

        System.out.println("ct: " + ServerHello_Packet.geteSKey());
        System.out.println("iv: " + ServerHello_Packet.getIv());
        System.out.println("user: " + user);
        System.out.println("session name : " + ServerHello_Packet.getsName());    
        System.out.println("session key: " + Base64.getEncoder().encodeToString(sessionKeyClient));
        //Decrypt nonce c
        byte[] checkNonceCBytes = ClientSessionKeyDecryption.decrypt(ServerHello_Packet.geteSKey(), ServerHello_Packet.getIv(), user, sessionKeyClient, ServerHello_Packet.getsName());
        if (Arrays.equals(checkNonceCBytes, nonceCBytes)) {
            // Get nonce S ready for encryption and add to cache
            String stringNonceS = ServerHello_Packet.getNonce();
            System.out.println(stringNonceS);
            byte[] usedNonceSBytes = Base64.getDecoder().decode(stringNonceS);
            nc.addNonce(usedNonceSBytes);
            // Fresh nonce R
            byte[] nonceBytesR = nc.getNonce();
            String nonceR = Base64.getEncoder().encodeToString(nonceBytesR);

            // Encrypt nonce S
            byte[] encNonceS = ClientSessionKeyEncryption.encrypt(sessionKeyClient, usedNonceSBytes, user, ServerHello_Packet.getsName());
            // Packet everything together to send to echo server
            ClientResponse clientResponse_packet = new ClientResponse(nonceR, user, Base64.getEncoder().encodeToString(ClientSessionKeyEncryption.getRawIv()), Base64.getEncoder().encodeToString(encNonceS));

            // Send packet off
            Socket socket2 = Communication.connectAndSend(host.getAddress(), host.getPort(), clientResponse_packet);
            //MESSAGE 4: Client recieves status
            HandshakeStatus handshakeStatus_packet = (HandshakeStatus) Communication.read(socket2);
            // If message returns true
            if (handshakeStatus_packet.getMsg() == true) {
                // Handshake protocol checks out
                System.out.println("done");
                return true;
            } else {
                //Otherwise false, exit system
                System.exit(0);
            }

        }
        return false;

    }
    
    
}
