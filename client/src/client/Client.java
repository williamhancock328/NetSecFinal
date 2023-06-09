package client;

import ClientServerCrypto.ClientMasterKeyDecryption;
import ClientServerCrypto.ClientSessionKeyDecryption;
import ClientServerCrypto.ClientSessionKeyEncryption;
import packets.*;
import javax.net.ssl.SSLSocket;
import communication.*;
import conf.Config;
import conf.Host;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import merrimackutil.util.NonceCache;
import merrimackutil.util.Tuple;
import packets.abstractpk.SessionKeyPackets;
import packets.filepack.FileCreate;
import packets.filepack.FileReceived;
import packets.filepack.FileSearchRequest;
import packets.filepack.FileSearchResponse;
import packets.filepack.FileSearchSendRequest;
import packets.filepack.FileSend;
import sse.Token;
import sse.client.FileNameSymmetricCrypto;
import sse.client.FileSymmetricCrypto;
import sse.client.SecretKeyGenerator;
import sse.client.Tokenizer;
import sse.transport.TransportManager;

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

    private static TransportManager transportManager;
    
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
        
        // Construct a new Transport Manager
        transportManager = new TransportManager();

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
                    //System.out.println("Now we have the ticket.");
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
            sessionKeyClient = ClientMasterKeyDecryption.decrypt(sessKeyResp_Packet.geteSKeyAlice(), sessKeyResp_Packet.getuIv(), user, pass, sessKeyResp_Packet.getCreateTime(), sessKeyResp_Packet.getValidityTime(), sessKeyResp_Packet.getsName());
            //.out.println("Client session key: " + Arrays.toString(sessionKeyClient));

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
        //.out.println(theHost.getAddress() + theHost.getPort());
        SSLSocket out = Communication.connectAndSend(theHost.getAddress(), theHost.getPort(), hi); // Send the packet

        //MESSAGE 3: Recieved the server hello
        ServerHello ServerHello_Packet = (ServerHello) Communication.read(out);

        //.out.println("session key: " + Base64.getEncoder().encodeToString(sessionKeyClient));

        //Decrypt nonce c
        byte[] checkNonceCBytes = ClientSessionKeyDecryption.decrypt(ServerHello_Packet.geteSKey(), ServerHello_Packet.getIv(), user, sessionKeyClient, ServerHello_Packet.getsName());
        if (Arrays.equals(checkNonceCBytes, nonceCBytes)) {
            // Get nonce S ready for encryption and add to cache
            String stringNonceS = ServerHello_Packet.getNonce();
            //.out.println(stringNonceS);
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
                //.out.println("done");
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
    private static boolean CommPhase() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchMethodException {

        //Welcome Message
        System.out.println("Welcome to the Cloud Server!");

        String filePass; //Initialize filePass
        //Initialize console and scanner objs
        Console console = System.console();
        //Scanner scanner = new Scanner(System.in);
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
        switch (input) {
            //Sending files
            case 1: {
                // Get fresh nonce D for MSG
                byte[] nonceDBytes = nc.getNonce();
                //Add nonceD to nonce cache
                nc.addNonce(nonceDBytes);

                //File location
                System.out.println("Enter location of file you wish to send, Please keep the file size too under 50KB:");
                String fileLocation = scanner2.nextLine();
                Path path = Paths.get(fileLocation);
                boolean exists = Files.exists(path);

                // If valid path config
                if (exists) {
                    // Construct the file
                    File file = new File(fileLocation);

                    //File password
                    filePass = new String(console.readPassword("Create a file password: "));
                    
                    //TO-DO: Encrypt file contents with file key
                    //Keywords used to associate a file with (searchable encryptin)
                    System.out.println("Enter associated key words, seperate with spaces: ");
                    String keywords = scanner2.nextLine();
                    String[] strings = keywords.split(" ");
                    ArrayList<String> keywordList = new ArrayList<>(Arrays.asList(strings));
                                        
                    //Ask for users who should have access to this file 
                    System.out.println("Add users who should have access to this file: ");
                    String users = scanner2.nextLine();
                    String[] users_strings = users.split(" ");
                    ArrayList<String> usersList = new ArrayList<>(Arrays.asList(users_strings));
                    if(!usersList.contains(user)) { // If this user is not in usersList
                        usersList.add(user);
                    }
                    
                    
                    // Assure there exists keywords
                    if(keywordList.isEmpty())
                        throw new IllegalArgumentException("Keywords must be present for the SEE in the cloud-service.");

                    // Starting SSE code here - Alex
                    
                    // 1. Construct the FP_Key and FP_IV
                    Tuple<SecretKey, String> fp_pair = SecretKeyGenerator.genKey(filePass);
                    SecretKey fpKey = fp_pair.getFirst();
                    String fpIV = fp_pair.getSecond();
                    
                    // 2. Convert the Keywords into Tokens w/ Sym. Encryption.
                    List<Token> tokens = Tokenizer.tokenize(keywordList, fpKey, fpIV);
                    List<String> tokens_strings = tokens.stream().map(n -> n.getValue()).collect(Collectors.toList());
                    
                    // 3. Encrypt the contents of the file
                    String encoded_file = FileSymmetricCrypto.encrypt(Files.readAllBytes(path), fpKey, fpIV);
                    
                    // 4. Encrypt File.name
                    System.out.println("File name: " + file.getName());
                    String encrypted_filename = FileNameSymmetricCrypto.encryptFileName(file.getName(), fpKey, fpIV);
                    
                    // 5. Consrtuct & Send the FileCreate Packet
                    FileCreate fileCreate = new FileCreate(encrypted_filename, usersList, tokens_strings);
                    
                    // Encode the file 
                    String encoded = Base64.getEncoder().encodeToString(fileCreate.serialize().getBytes());                    
                    byte[] bytePacket = Base64.getDecoder().decode(encoded);
                    
                    //System.out.println("Byte packet pre encryption:" + encoded);
                    
                    // Encrypt file create with session key here
                    byte[] encPacket = ClientSessionKeyEncryption.encrypt(sessionKeyClient, bytePacket, user, service);
                    
                    
                    String StringEncPacket = Base64.getEncoder().encodeToString(encPacket);
                    //System.out.println("enc pkt: " + StringEncPacket);
                    byte[] rawIV = ClientSessionKeyEncryption.getRawIv();
                    String StringRawIV = Base64.getEncoder().encodeToString(rawIV);
                    byte[] EncNonce = ClientSessionKeyEncryption.encrypt(sessionKeyClient, nonceDBytes, user, service);
                    String StringEncNonce = Base64.getEncoder().encodeToString(EncNonce);
                    
                    SessionKeyPackets SessionKeyPacket_packet = new SessionKeyPackets(StringRawIV, StringEncNonce, StringEncPacket, user);
                    SSLSocket out = Communication.connectAndSend(hostt.getAddress(), hostt.getPort(), SessionKeyPacket_packet); // Send the packet
                    FileReceived fileReceived = (FileReceived) Communication.read(out); // Receive the fileReceived packet
                                        
                    // 6. Initilialize the FileSend stream.
                    List<FileSend> fileSends = transportManager.fromEncodedFile(fileReceived.getFileID(), encoded_file);
                    
                    for(int i = 0; i < fileSends.size(); i++) {
                        FileSend packet = fileSends.get(i); // Get the next packet
                        
                        // Encode keywords
                        String encoded2 = Base64.getEncoder().encodeToString(packet.serialize().getBytes());
                        byte[] bytePacket2 = Base64.getDecoder().decode(encoded2);
                        
                        //Encrypt file create with session key here
                        byte[] encPacket2 = ClientSessionKeyEncryption.encrypt(sessionKeyClient, bytePacket2, user, service);
                        String StringEncPacket2 = Base64.getEncoder().encodeToString(encPacket2);
                        byte[] rawIV2 = ClientSessionKeyEncryption.getRawIv();
                        String StringRawIV2 = Base64.getEncoder().encodeToString(rawIV2);
                        byte[] EncNonce2 = ClientSessionKeyEncryption.encrypt(sessionKeyClient, nonceDBytes, user, service);
                        String StringEncNonce2 = Base64.getEncoder().encodeToString(EncNonce2);
                   
                        SessionKeyPackets SessionKeyPacket_packet2 = new SessionKeyPackets(StringRawIV2, StringEncNonce2, StringEncPacket2, user);
                        
                        SSLSocket send_out = Communication.connectAndSend(hostt.getAddress(), hostt.getPort(), SessionKeyPacket_packet2); // Send the packet
                        FileReceived send_fileReceived = (FileReceived) Communication.read(send_out); // Receive the fileReceived packet
                    }
                    
                    // Done! File should now be updated
                    System.out.println("Cloud Server Document ID: " + fileReceived.getFileID());
                    
                    
                } else {

                    System.out.println("The file path is invalid.");
                    break;
                }
            }; break;

            // Request Files
            case 2: {
                
               //TO-DO: Encrypt file contents with file key
               //Keywords used to associate a file with (searchable encryptin)
               System.out.println("Please enter the associated key words: ");
               String keywords = scanner2.nextLine();
               String[] strings = keywords.split(" ");
               ArrayList<String> keywordList = new ArrayList<>(Arrays.asList(strings));
               
               // Assure there exists keywords
               if(keywordList.isEmpty())
                    throw new IllegalArgumentException("Keywords must be present for the SEE in the cloud-service.");
               
               //File password
               filePass = new String(console.readPassword("Please enter your file password: "));
                    
               //File location
               System.out.println("Please enter the path where you would like your file to be saved: ");
               String fileLocation = scanner2.nextLine();
               Path path = Paths.get(fileLocation);               
               // Starting SSE code here - Alex

               // 1. Construct the FP_Key and FP_IV
               Tuple<SecretKey, String> fp_pair = SecretKeyGenerator.genKey(filePass);
               SecretKey fpKey = fp_pair.getFirst();
               String fpIV = fp_pair.getSecond();
               
               // 2. Convert the Keywords into Tokens w/ Sym. Encryption.
               List<Token> tokens = Tokenizer.tokenize(keywordList, fpKey, fpIV);
               List<String> tokens_strings = tokens.stream().map(n -> n.getValue()).collect(Collectors.toList());
               
               // 3. Send a FileSearchRequest
               FileSearchRequest fileSearchRequest = new FileSearchRequest(tokens_strings, user);
               SSLSocket send_out = Communication.connectAndSend(hostt.getAddress(), hostt.getPort(), fileSearchRequest); // Send the packet
               FileSearchResponse fileSearchResponse_packet = (FileSearchResponse) Communication.read(send_out); // Receive the FileSearchResponse packet.
               boolean accessed = fileSearchResponse_packet.isAccessed(); 
               
               // No files have been found with the associating keywords, or you were not permitted to access a file.
               if(!accessed)  {
                   System.out.println("No files have been found with the associating keywords, or you were not permitted to access a file.");
                   break;
               } 
               
               // We must have found an accepting document if we made it here.
               final String ID = fileSearchResponse_packet.getID();
               final String plain_text_fileName = FileNameSymmetricCrypto.decryptFileName(fileSearchResponse_packet.getEncrypted_filename(), fpKey, fpIV);
               
               // If the plain_text_fileName is null that means that the keys were incorect, and thus there is a difference between the keys for the fpKey, fpIV 
               //   .. with the keywords and the filename/file.
               if(plain_text_fileName == null) {
                   System.out.println("Error decrypting file, probabily an incorrect key.");
                   break;
               }
               // If the file exists at the path directory, then do not add it, duplicate files
               File file = new File(path.toString() + "/" +plain_text_fileName);
               if(file.exists()) {
                   System.out.println("A file already exists at the path ["+file.getPath()+"]");
                   break;
               }
               
               System.out.println("Accessed: " + accessed + " ID:" + ID + "FileName: " + plain_text_fileName);
               System.out.println("Starting file transfer...");
               
               // Builder that constructs the encoded_file from the FileSend packets
               StringBuilder builder = new StringBuilder();
               
               // Run until a packet with ending isfinal = true
               boolean run = true;
               int last_index = -1;
               while(run) {
                   // Construct a FileSearchSendRequest packet, and receive a FileSend packet containing the fragment.
                   FileSearchSendRequest fssr_send_Packet = new FileSearchSendRequest(ID, last_index);
                   SSLSocket search_out = Communication.connectAndSend(hostt.getAddress(), hostt.getPort(), fssr_send_Packet); // Send the packet
                   FileSend fileSend_Resp = (FileSend) Communication.read(search_out); // Receive the FileSend packet                   
                   
                   // If the file fragment is null it was not found.
                   if(fileSend_Resp == null || fileSend_Resp.getFile_bit().isEmpty()) {
                       throw new NullPointerException("File bit null, file bit was not found on server.\nFile in EncryptedDocument database is fragmented or there was an issue sending the file.");
                   }
                   
                   // Append the fragment to the builder
                   builder.append(fileSend_Resp.getFile_bit());
                   
                   // Increase last_index ctr.
                   last_index = fileSend_Resp.getIndex();
                   
                   // If this is the last file, stop the loop
                   if(fileSend_Resp.isIsfinal()) { run = false; }
                   
               }
               
               // Decrypt the file
               byte[] plaintext_file = FileSymmetricCrypto.decrypt(builder.toString(), fpKey, fpIV);            
               
               // Create the file
               file.createNewFile();
               
               // Write the file to the location https://www.baeldung.com/java-write-byte-array-file
               try(FileOutputStream outputStream = new FileOutputStream(file)) {
                    outputStream.write(plaintext_file);
               } catch(Exception e) {
                   System.out.println("There was an error writing the file at path ["+file.getPath()+"]");
                   e.printStackTrace();
               }
               
               // If the file wrote correctly, print the output
               System.out.println("File create successfully at path ["+file.getPath()+"]");
               
            }; break;
            
            //Exit
            case 3: {
                System.exit(0);
            }; break;
            default:
                System.out.println("Invalid input.");
                break;
        }

        return true;
    }

}
