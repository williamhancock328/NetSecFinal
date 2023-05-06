package KDCServer;

/**
 *
 * @author Mark Case, William Hancock, Alexander Elguezabal
 */
import KDCServer.config.Config;
import KDCServer.config.Secrets;
import KDCServer.config.SecretsConfig;
import KDCServer.crypto.ServerMasterKeyEncryption;
import communication.Communication;
import config.SSLConfig;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import json.PwValue;
import json.Vault;
import merrimackutil.cli.LongOption;
import merrimackutil.cli.OptionParser;
import merrimackutil.codec.Base32;
import merrimackutil.util.NonceCache;
import merrimackutil.util.Tuple;
import packets.AuthRequest;
import packets.EnrollRequest;
import packets.Packet;
import static packets.PacketType.AuthRequest;
import packets.ServerResponse;
import packets.SessionKeyRequest;
import packets.SessionKeyResponse;
import twoauth.Server;
import twoauth.scrypt;
import twoauth.totp;

public class KDCServer {

    public static ArrayList<Secrets> secrets = new ArrayList<>();
    private static SecretsConfig secretsConfig;
    private static Config config;
    private static SSLConfig sslconfig;
    private static Vault v;
    private static SSLServerSocket server;

    public static void main(String[] args) throws NoSuchAlgorithmException, FileNotFoundException, InvalidObjectException, IOException, InvalidKeySpecException {

        OptionParser op = new OptionParser(args);
        LongOption[] ar = new LongOption[3];
        ar[0] = new LongOption("config", true, 'c');
        ar[1] = new LongOption("auth", true, 'a');
        ar[2] = new LongOption("help", false, 'h');
        op.setLongOpts(ar);
        op.setOptString("hc:a:");
        Tuple<Character, String> opt = op.getLongOpt(false);
        if (opt == null || Objects.equals(opt.getFirst(), 'h')) {
            System.out.println("usage:\n"
                    + "kdcd\n"
                    + " kdcd --config <configfile>\n"
                    + " kdcd --help\n"
                    + "options:\n"
                    + " -c, --config Set the config file.\n"
                    + " -a, --auth Set the auth config file.\n"
                    + " -h, --help Display the help.");
            System.exit(0);
        } else if (Objects.equals(opt.getFirst(), 'c')) {
            // Initialize config
            config = new Config(opt.getSecond());
            opt = op.getLongOpt(false);
            if (Objects.equals(opt.getFirst(), 'a')) {
                sslconfig = new SSLConfig(opt.getSecond());
                // Initialize the Secrets config from the path "secrets_file" of config.
                secretsConfig = new SecretsConfig(config.getSecrets_file());

                //init vault.
                try {
                    v = new Vault(sslconfig.getPassword_file());
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.setProperty("javax.net.ssl.keyStore", sslconfig.getKeystore_file());
                System.setProperty("javax.net.ssl.keyStorePassword", sslconfig.getKeystore_pass());

                try {
                    SSLServerSocketFactory sslFact;
                    // Get a copy of the deafult factory. This is what ever the
                    // configured provider gives.
                    sslFact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

                    // Set up the server socket using the specified port number.
                    server = (SSLServerSocket) sslFact.createServerSocket(5000);

                    // Set the protocol to 1.3
                    server.setEnabledProtocols(new String[]{"TLSv1.3"});
                    // Accept packets & communicate
                    poll();

                    // Close the socket when polling is completed or an error is thrown.
                    server.close();

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    server.close();
                    System.out.println("KDC Server IOException error, closing down.");
                    System.exit(0);
                } catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                    server.close();
                    System.out.println("KDC Server IOException error, closing down.");
                    System.exit(0);
                }
            }

        }
    }

    // Gobal noncecache, holding nonces.
    private static NonceCache nc = new NonceCache(32, 30);
    private static ArrayList<byte[]> nonceList = new ArrayList<>();

    /**
     * Waits for a connection with a peer socket, then polls for a message being
     * sent. Each iteration of the loop operates for one message, as not to
     * block multi-peer communication.
     *
     * @throws IOException
     */
    private static void poll() throws IOException, NoSuchMethodException, NoSuchAlgorithmException, InvalidKeySpecException {
        while (true) { // Consistently accept connections

            // Establish the connection & read the message
            Socket peer = server.accept();

            // Determine the packet type.
            System.out.println("Waiting for a packet...");
            final Packet packet = Communication.read(peer);

            System.out.println("Packet Recieved: [" + packet.getType().name() + "]");

            // Switch statement only goes over packets expected by the KDC, any other packet will be ignored.
            switch (packet.getType()) {

                case AuthRequest: {
                    //check if a user exists, and authenticate them if they do.
                    AuthRequest AuthRequest_packet = (AuthRequest) packet;
                    String u = AuthRequest_packet.getUser();
                    String pw = AuthRequest_packet.getPass();
                    String receivedNonceB = AuthRequest_packet.getNonce();
                    byte[] nonceBToBytes = Base64.getDecoder().decode(receivedNonceB);

                    //Check if this is nonce we already recieved
                    if (!nc.containsNonce(nonceBToBytes)) {
                        //if not add it in as a now received nonce to the nonce cache
                        nc.addNonce(nonceBToBytes);
                        int otp = AuthRequest_packet.getOtp();
                        //check pw
                        PwValue vaultEnt = v.GetPW(u);
                        String hashToCheck = scrypt.checkPw(pw, Base64.getDecoder().decode(vaultEnt.getSalt()));
                        if (hashToCheck.equals(vaultEnt.getPass())) {
                        } else {
                            ServerResponse authres = new ServerResponse(false, "Authentication failed.", "");
                            Communication.send(peer, authres);
                            break;
                        }
                        //totp
                        totp t = new totp(vaultEnt.getTotpkey(), otp);
                        if (t.CheckOtp()) {
                            String toStringNonceB = Base64.getEncoder().encodeToString(nonceBToBytes);
                            ServerResponse authres = new ServerResponse(true, "", toStringNonceB);
                            Communication.send(peer, authres);
                            //authorization is successful
                            break;
                        } else {
                            ServerResponse authres = new ServerResponse(false, "Authentication failed.", "");
                            Communication.send(peer, authres);
                            break;
                        }

                    } else {
                        System.out.println("Replay attack detected");
                        System.exit(0);
                    }
                }

                case EnrollRequest: {
                    //check if a user exists, and enroll them if not.
                    EnrollRequest EnrollRequest_packet = (EnrollRequest) packet;
                    String user = EnrollRequest_packet.getUser();
                    String receivedNonceA = EnrollRequest_packet.getNonce();
                    System.out.println(receivedNonceA);
                    byte[] nonceAToBytes = Base64.getDecoder().decode(receivedNonceA);

                    //Check if this is nonce we already recieved
                    if (!nc.containsNonce(nonceAToBytes)) {

                        //if not add it in as a now received nonce to the nonce cache
                        nc.addNonce(nonceAToBytes);

                        String toStringNonceA = Base64.getEncoder().encodeToString(nonceAToBytes);

                        if (v.GetPW(user) != null) {
                            ServerResponse eresp = new ServerResponse(false, "User already exists.", "");
                            Communication.send(peer, eresp);
                            break;
                        }

                        // Construct an key for the HMAC.
                        KeyGenerator hmacKeyGen = KeyGenerator.getInstance("HmacSHA1");
                        SecretKey key = hmacKeyGen.generateKey();
                        byte[] totpbytes = key.getEncoded();

                        //hash the password
                        Tuple<SecretKey, byte[]> userToAdd = scrypt.genKey(EnrollRequest_packet.getPass());
                        String phash = Base64.getEncoder().encodeToString(userToAdd.getFirst().getEncoded());
                        String salt = Base64.getEncoder().encodeToString(userToAdd.getSecond());
                        String totpkey = Base64.getEncoder().encodeToString(totpbytes);
                        v.AddPW(salt, phash, totpkey, user);
                        //save the vault
                        v.SaveJSON();
                        ServerResponse res = new ServerResponse(true, Base32.encodeToString(totpbytes, true), toStringNonceA);
                        Communication.send(peer, res);
                        break;

                    } else {
                        System.out.println("Replay attack detected");
                        System.exit(0);
                    }

                }

                case SessionKeyRequest: {
                    SessionKeyRequest SessionKeyRequest_packet = (SessionKeyRequest) packet; //Receive packet containing username and service name
                    String receivedNonceC = SessionKeyRequest_packet.getNonce();
                    System.out.println(receivedNonceC);
                    byte[] nonceCToBytes = Base64.getDecoder().decode(receivedNonceC);

                    if (!nc.containsNonce(nonceCToBytes)) {
                        nc.addNonce(nonceCToBytes);
                        String sessionName = "";
                        String user = "";
                        String pw = "";
                        String svcpw = "";
                        for (Secrets secret : secrets) {
                            if (secret.getUser().equalsIgnoreCase(SessionKeyRequest_packet.getuName())) {
                                System.out.println("Secret pw associated with user: " + secret.getUser());
                                pw = secret.getSecret();
                                user = secret.getUser();
                                sessionName = SessionKeyRequest_packet.getsName();
                                //sendSessionKey(user, sessionName, pw);
                                break;
                            }
                        }
                        for (Secrets secret : secrets) {
                            if (secret.getUser().equalsIgnoreCase(SessionKeyRequest_packet.getsName())) {
                                System.out.println("Secret pw associated with user: " + secret.getUser());
                                svcpw = secret.getSecret();
                                sessionName = SessionKeyRequest_packet.getsName();
                                //sendSessionKey(user, sessionName, pw);
                                break;
                            }

                        }

                        //SessionKeyResponse chapStatus_packet = new SessionKeyResponse(sendSessionKey(user, sessionName, pw));
                        Communication.send(peer, sendSessionKey(user, sessionName, pw, svcpw, receivedNonceC));
                    } else {
                        System.out.println("Replay attack detected");
                        System.exit(0);
                    }
                }

                ;
                break;

            }
        }

    }

    //this is the part where session key is sent to client 
    //this is the part where session key is sent to client 
    private static SessionKeyResponse sendSessionKey(String uname, String sName, String pw, String svcpw, String nonce) {
        //validity period comes from config file  

        try {
            final long ctime = System.currentTimeMillis();

            // Get a key generator object.
            KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");

            // Set the key size to 128 bits.
            aesKeyGen.init(128);

            // Generate the session key.
            SecretKey aesKey = aesKeyGen.generateKey();
            Tuple<byte[], byte[]> skeyiv = ServerMasterKeyEncryption.encrypt(svcpw, config.getValidity_period(), ctime, sName, sName, sName, aesKey);
            Tuple<byte[], byte[]> ukeyiv = ServerMasterKeyEncryption.encrypt(pw, config.getValidity_period(), ctime, uname, sName, uname, aesKey);
            SessionKeyResponse toSend = new SessionKeyResponse(Base64.getEncoder().encodeToString(ukeyiv.getSecond()), Base64.getEncoder().encodeToString(ukeyiv.getFirst()), ctime, config.getValidity_period(), uname, sName, Base64.getEncoder().encodeToString(skeyiv.getSecond()), Base64.getEncoder().encodeToString(skeyiv.getFirst()), nonce);
            //now we send!
            return toSend;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | InvalidKeySpecException ex) {
            Logger.getLogger(KDCServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;

    }

}
