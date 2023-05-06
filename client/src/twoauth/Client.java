package twoauth;

import ClientServerCrypto.ClientMasterKeyDecryption;
import ClientServerCrypto.ClientSessionKeyDecryption;
import ClientServerCrypto.ClientSessionKeyEncryption;
import ClientServerCrypto.scrypt;
import packets.*;
import javax.net.ssl.SSLSocket;
import communication.*;
import conf.Config;
import conf.Host;
import java.io.Console;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import javax.crypto.SecretKey;
import merrimackutil.util.NonceCache;

/**
 *
 * @author William H., Mark C., Alex E.
 */
public class Client {

    public static ArrayList<Host> hosts = new ArrayList<>(); //Lsit of servers (hosts)
    private static Config config; // config files
    private static String host = "hosts.json"; //host file
    private static String port; //port num
    private static String pass; // user pass
    private static String user; // usernmae
    Console console = System.console(); //console class for entering passwords
    private static NonceCache nc = new NonceCache(32, 30); //Nonce cache for msg freshness
    private static byte[] sessionKeyClient; //Session key, client side
    private static String service = "cloudservice"; // Service name 

    /**
     * Client Side Communication Goal is to establish trust with KDC and gain
     * access to cloud-server.From there, send or request files
     *
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.NoSuchMethodException
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.spec.InvalidKeySpecException
     * @throws javax.crypto.NoSuchPaddingException
     * @throws java.security.InvalidKeyException
     * @throws javax.crypto.IllegalBlockSizeException
     * @throws javax.crypto.BadPaddingException
     * @throws java.security.InvalidAlgorithmParameterException
     */
    public static void main(String[] args) throws IOException, NoSuchMethodException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        //print a welcome message then a menu with the options to create a user, download a file, upload a file, manage tags, and search for files by tag
        System.out.println("Welcome to the File Sharing System!");
        //Console and scanner objects
        Console console = System.console();
        Scanner scanner = new Scanner(System.in);

        config = new Config(host); //Config to hosts.json

        System.out.println("Login Menu:");
        System.out.println("1. Create a new user");
        System.out.println("2. Login as an existing user");
        System.out.println("3. Exit");
        Host theHost = getHost("kdcd");
        Scanner scanner2 = new Scanner(System.in);
        int input = scanner2.nextInt();
        //Grab input from user
        switch (input) {
            //New user
            case 1:
                System.out.println("Please enter the username you would like to use: ");
                String newuser = scanner.nextLine();
                String newpass = new String(console.readPassword("Enter password:"));
                //Calls create method
                create(host, newpass, theHost.getPort(), newuser);
                break;
            // Returning user
            case 2:
                System.out.println("Please enter your username: ");
                user = scanner.nextLine();
                pass = new String(console.readPassword("Enter password:"));
                System.out.println("Please enter your one time password: ");
                int otp = scanner.nextInt();
                //Calls auth method, if it's true -> then request a ticket to cloud-server and begin handshake
                if (auth(user, pass, theHost.getPort(), user, otp)) {
                    System.out.println("");
                    Ticket tik = SessionKeyRequest();
                    System.out.println("Now we have the ticket.");
                    Handshake(tik);
                } else {
                    System.out.println("Login failed.");
                }
                break;
            //Exit
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
        // Get fresh nonce A
        byte[] nonceABytes = nc.getNonce();
        //Add nonceA to nonce cache
        nc.addNonce(nonceABytes);
        // Convert nonceABytes to Base64 string format
        String nonceA = Base64.getEncoder().encodeToString(nonceABytes);

        EnrollRequest send = new EnrollRequest(user, pass, nonceA);
        SSLSocket out = Communication.connectAndSend(host, port, send);
        final Packet packet = Communication.read(out);
        ServerResponse ServerResponse_packet = (ServerResponse) packet;
        String checkNonce = ServerResponse_packet.getNonce();
        if (ServerResponse_packet.getStatus() && nonceA.equals(checkNonce)) {
            System.out.println("Nonce Matched");
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
        // Get fresh nonce B
        byte[] nonceBBytes = nc.getNonce();
        //Add nonceA to nonce cache
        nc.addNonce(nonceBBytes);
        // Convert nonceABytes to Base64 string format
        String nonceB = Base64.getEncoder().encodeToString(nonceBBytes);

        AuthRequest send = new AuthRequest(user, pass, otp, nonceB);
        SSLSocket out = Communication.connectAndSend(host, port, send);
        final Packet packet = Communication.read(out);
        ServerResponse ServerResponse_packet = (ServerResponse) packet;
        String checkNonce = ServerResponse_packet.getNonce();
        if (ServerResponse_packet.getStatus() && nonceB.equals(checkNonce)) {
            System.out.println("Nonce Matched");
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

    /*
    Get host method
     */
    private static Host getHost(String host_name) {
        return hosts.stream().filter(n -> n.getHost_name().equalsIgnoreCase(host_name)).findFirst().orElse(null);
    }

    /*
    Session key request method
    Grants authenticated user access to
    cloud-server
     */
    private static Ticket SessionKeyRequest() throws IOException, NoSuchMethodException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
        Host theHost = getHost("kdcd");

        // Get fresh nonce C
        byte[] nonceCBytes = nc.getNonce();
        //Add nonceA to nonce cache
        nc.addNonce(nonceCBytes);
        // Convert nonceABytes to Base64 string format
        String nonceC = Base64.getEncoder().encodeToString(nonceCBytes);

        // MESSAGE 1: Client sends kdc username and service name
        SessionKeyRequest req = new SessionKeyRequest(user, service, nonceC); // Construct the packet
        SSLSocket out = Communication.connectAndSend(theHost.getAddress(), theHost.getPort(), req); // Send the packet

        // MESSAGE 2
        SessionKeyResponse sessKeyResp_Packet = (SessionKeyResponse) Communication.read(out); // Read for a packet  // KDC checks username validity and if valid, demands password and gives a nonce
        String checkNonce = sessKeyResp_Packet.getNonce();

        if (checkNonce.equals(nonceC)) {
            System.out.println("Nonce matched");
            sessionKeyClient = ClientMasterKeyDecryption.decrypt(sessKeyResp_Packet.geteSKeyAlice(), sessKeyResp_Packet.getuIv(), user, pass, sessKeyResp_Packet.getCreateTime(), sessKeyResp_Packet.getValidityTime(), sessKeyResp_Packet.getsName());
            System.out.println("Client session key: " + Arrays.toString(sessionKeyClient));

        } else {
            System.out.println("Replay attack detected");
            System.exit(0);
        }
        //close connection to KDC
        out.close();

        //send the ticket
        return new Ticket(sessKeyResp_Packet.getCreateTime(), sessKeyResp_Packet.getValidityTime(), sessKeyResp_Packet.getuName(), sessKeyResp_Packet.getsName(), sessKeyResp_Packet.getIv(), sessKeyResp_Packet.geteSKey());
    }

    /*
    Handshake method.
    Establishes trust between client and cloud-server
     */
    private static boolean Handshake(Ticket in) throws IOException, NoSuchMethodException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        //Client connects to cloud service
        Host theHost = getHost("cloudservice");

        // Get fresh nonce C
        byte[] nonceCBytes = nc.getNonce();
        // Convert nonceCBytes to Base64 string format
        String nonceC = Base64.getEncoder().encodeToString(nonceCBytes);

        // Serialize ticket that we will send
        String tkt = in.serialize();

        // MESSAGE 1: Client sends cloudservice the nonce C and ticket
        ClientHello hi = new ClientHello(nonceC, tkt); // Construct the packet
        System.out.println(theHost.getAddress() + theHost.getPort());
        SSLSocket out = Communication.connectAndSend(theHost.getAddress(), theHost.getPort(), hi); // Send the packet

        //MESSAGE 3: Recieved the server hello
        ServerHello ServerHello_Packet = (ServerHello) Communication.read(out);

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
            SSLSocket out2 = Communication.connectAndSend(theHost.getAddress(), theHost.getPort(), clientResponse_packet);

            //MESSAGE 4: Client recieves status
            HandshakeStatus handshakeStatus_packet = (HandshakeStatus) Communication.read(out2);
            // If message returns true
            if (handshakeStatus_packet.getMsg() == true) {
                // Handshake protocol checks out
                System.out.println("done");
                CommPhase(); //Begin Communication phase between cloud and user
            } else {
                //Otherwise false, exit system
                System.exit(0);
            }

        }
        return false;

    }

    /*
    Communication phase.
    This is where send and request files take place
     */
    private static boolean CommPhase() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        //Welcome Message
        System.out.println("Welcome to the Cloud Server!");

        String filePass; //Initialize filePass
        //Initialize console and scanner objs
        Console console = System.console();
        Scanner scanner = new Scanner(System.in);
        //Print options
        System.out.println("Options Menu:");
        System.out.println("1. Send Files");
        System.out.println("2. Request Files");
        System.out.println("3. Exit");
        //Read input
        Scanner scanner2 = new Scanner(System.in);
        int input = scanner2.nextInt();
        scanner2.nextLine();
        Host hostt = getHost("cloudservice");
        Host hosttt = getHost("cloudservice");
        switch (input) {
            //Sending files
            case 1:
                // Get fresh nonce D for MSG
                byte[] nonceDBytes = nc.getNonce();
                //Add nonceD to nonce cache
                nc.addNonce(nonceDBytes);
                // Convert nonceDBytes to Base64 string format
                String nonceD = Base64.getEncoder().encodeToString(nonceDBytes);

                //File location
                System.out.println("Enter location of file you wish to send:");
                String fileLocation = scanner2.nextLine();
                Path path = Paths.get(fileLocation);
                boolean exists = Files.exists(path);

                if (exists) {
//File password
                    filePass = new String(console.readPassword("Create a file password:"));
                    SecretKey fileKey = scrypt.genKey(filePass, user); //SecretKey to encrypt file with
                    byte[] fileKeyBytes = fileKey.getEncoded();
                    System.out.println(Base64.getEncoder().encodeToString(fileKeyBytes));

//TO-DO: Encrypt file contents with file key
//File keywords
                    System.out.println("Create associated key words. Please separate each word with a comma:");
                    String keywords = scanner2.nextLine();
                    String[] strings = keywords.split(",");
                    ArrayList<String> keywordList = new ArrayList<>(Arrays.asList(strings));

// Encode the file key as a base64 string
                    String fileKeyBase64 = Base64.getEncoder().encodeToString(fileKeyBytes);
                    System.out.println(fileKeyBase64);

// Encode the keywords as a single string using a comma separator
                    String encodedKeywords = String.join(",", keywordList);
                    byte[] byteKeyWords = Base64.getEncoder().encode(encodedKeywords.getBytes());

                    byte[] EncNonce = ClientSessionKeyEncryption.encrypt(sessionKeyClient, nonceDBytes, user, service);
                    String StringEncNonce = Base64.getEncoder().encodeToString(EncNonce);

                    byte[] EncKeyWords = ClientSessionKeyEncryption.encrypt(sessionKeyClient, byteKeyWords, user, service);
                    String StringEncKeyWords = Base64.getEncoder().encodeToString(EncKeyWords);

// MESSAGE 1: Client sends KeyWords for file send
                    KeyWordSend sendKeyWords = new KeyWordSend(StringEncKeyWords, StringEncNonce); // Construct the packet
                    SSLSocket out = Communication.connectAndSend(hostt.getAddress(), hostt.getPort(), sendKeyWords); // Send the packet


                } else {
                    System.out.println("The file path is invalid.");
                    break;
                }
                break;
            // Request Files
            case 2:

                // Get fresh nonce E for MSG
                byte[] nonceEBytes = nc.getNonce();
                //Add nonceE to nonce cache
                nc.addNonce(nonceEBytes);
                // Convert nonceEBytes to Base64 string format
                String nonceE = Base64.getEncoder().encodeToString(nonceEBytes);

                //File keywords
                System.out.println("Enter key words. Please seperate each one with a comma");
                String keywords2 = scanner2.nextLine();
                String[] strings2 = keywords2.split(",");
                ArrayList<String> keywordList2 = new ArrayList<>(Arrays.asList(strings2));
                String toStringArray = keywordList2.toString();

                // MESSAGE 1: Client sends KeyWords for file request
                KeyWordRequest KeyWordRequest_packet = new KeyWordRequest(toStringArray, nonceE); // Construct the packet
                SSLSocket out = Communication.connectAndSend(hosttt.getAddress(), hosttt.getPort(), KeyWordRequest_packet); // Send the packet

                break;
            //Exit
            case 3:
                System.exit(0);
                break;
            default:
                System.out.println("Invalid input.");
                break;
        }

        return true;
    }

}
