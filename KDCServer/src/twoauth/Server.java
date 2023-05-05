package twoauth;

import packets.AuthRequest;
import communication.Communication;
import packets.Packet;
import packets.EnrollRequest;
import packets.ServerResponse;
import config.SSLConfig;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import json.PwValue;
import json.Vault;
import merrimackutil.cli.LongOption;
import merrimackutil.cli.OptionParser;
import merrimackutil.util.Tuple;
import merrimackutil.codec.Base32;

/**
 * The server class for the TwoAuth system.
 *
 * @author willi
 */
public class Server {

    private static SSLConfig config;
    private static Vault v;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, InvalidObjectException, NoSuchMethodException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Parse the command line arguments.
        OptionParser op = new OptionParser(args);
        LongOption[] ar = new LongOption[2];
        ar[0] = new LongOption("config", true, 'c');
        ar[1] = new LongOption("help", false, 'h');
        op.setLongOpts(ar);
        op.setOptString("hc:");
        Tuple<Character, String> opt = op.getLongOpt(false);
        if (opt == null || Objects.equals(opt.getFirst(), 'h')) {
            System.out.println("usage:\n"
                    + "authserver\n"
                    + "authserver --config <configfile>\n"
                    + "authserver --help\n"
                    + "options:\n"
                    + "-c, --config Set the config file.\n"
                    + "-h, --help Display the help.");
            System.exit(0);
        } else if (Objects.equals(opt.getFirst(), 'c')) {
            // Initialize config
            config = new SSLConfig(opt.getSecond());
        }
        // Set up the server socket.
        SSLServerSocketFactory sslFact;
        SSLServerSocket server;
        //init vault.
        try {
            v = new Vault(config.getPassword_file());
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Set the keystore and keystore password.
        System.setProperty("javax.net.ssl.keyStore", config.getKeystore_file());
        System.setProperty("javax.net.ssl.keyStorePassword", config.getKeystore_pass());

        try {
            // Get a copy of the deafult factory. This is what ever the
            // configured provider gives.
            sslFact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

            // Set up the server socket using the specified port number.
            server = (SSLServerSocket) sslFact.createServerSocket(5000);

            // Set the protocol to 1.3
            server.setEnabledProtocols(new String[]{"TLSv1.3"});

            // Loop forever handing connections.
            while (true) {
                // Wait for a connection.
                Socket sock = server.accept();

                System.out.println("Connection received.");

                System.out.println("Waiting for a packet...");
                final Packet packet = Communication.read(sock);

                System.out.println("Packet Recieved: [" + packet.getType().name() + "]");
                //state machine code
                switch (packet.getType()) {
                    case EnrollRequest:
                        //check if a user exists, and enroll them if not.
                        EnrollRequest EnrollRequest_packet = (EnrollRequest) packet;
                        String user = EnrollRequest_packet.getUser();
                        if (v.GetPW(user) != null) {
                            ServerResponse eresp = new ServerResponse(false, "User already exists.");
                            send(eresp, sock);
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
                        ServerResponse res = new ServerResponse(true, Base32.encodeToString(totpbytes, true));
                        send(res, sock);
                        break;

                    case AuthRequest:
                        //check if a user exists, and authenticate them if they do.
                        AuthRequest AuthRequest_packet = (AuthRequest) packet;
                        String u = AuthRequest_packet.getUser();
                        String pw = AuthRequest_packet.getPass();
                        int otp = AuthRequest_packet.getOtp();
                        //check pw
                        PwValue vaultEnt = v.GetPW(u);
                        String hashToCheck = scrypt.checkPw(pw, Base64.getDecoder().decode(vaultEnt.getSalt()));
                        if (hashToCheck.equals(vaultEnt.getPass())) {
                        } else {
                            ServerResponse authres = new ServerResponse(false, "Authentication failed.");
                            send(authres, sock);
                            break;
                        }
                        //totp
                        totp t = new totp(vaultEnt.getTotpkey(), otp);
                        if (t.CheckOtp()) {
                            ServerResponse authres = new ServerResponse(true, "");
                            send(authres, sock);
                            //authorization is successful
                            break;
                        } else {
                            ServerResponse authres = new ServerResponse(false, "Authentication failed.");
                            send(authres, sock);
                            break;
                        }
                }
                // Close the connection.
                sock.close();
                System.exit(0);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void send(ServerResponse toSend, Socket sock) throws IOException {
        Communication.send(sock, toSend);
    }

}
