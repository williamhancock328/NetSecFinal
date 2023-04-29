package twoauth;
import packets.*;
import java.util.Objects;
import javax.net.ssl.SSLSocket;
import merrimackutil.cli.LongOption;
import merrimackutil.cli.OptionParser;
import merrimackutil.util.Tuple;
import communication.*;
import java.io.Console;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author willi
 */
public class Client {

    private static String user;
    private static String host;
    private static String port;

    /**
     * The client for the TwoAuth system.
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, NoSuchMethodException {
        // Parse the command line arguments.
        OptionParser op = new OptionParser(args);
        LongOption[] ar = new LongOption[5];
        ar[0] = new LongOption("create", false, 'c');
        ar[1] = new LongOption("auth", false, 'a');
        ar[2] = new LongOption("user", true, 'u');
        ar[3] = new LongOption("host", true, 'h');
        ar[4] = new LongOption("port", true, 'p');
        op.setLongOpts(ar);
        op.setOptString("cau:h:p:");
        Tuple<Character, String> opt = op.getLongOpt(false);
        if (opt == null) {
            System.out.println("usage:\n"
                    + "authserver\n"
                    + "authserver --config <configfile>\n"
                    + "authserver --help\n"
                    + "options:\n"
                    + "-c, --config Set the config file.\n"
                    + "-h, --help Display the help.");
            System.exit(0);
            //check if we are creating or authenticating.
        } else if (Objects.equals(opt.getFirst(), 'c')) {
            opt = op.getLongOpt(false);
            if (Objects.equals(opt.getFirst(), 'u')) {
                user = opt.getSecond();
                opt = op.getLongOpt(false);
                if (Objects.equals(opt.getFirst(), 'h')) {
                    host = opt.getSecond();
                    opt = op.getLongOpt(false);
                    if (Objects.equals(opt.getFirst(), 'p')) {
                        port = opt.getSecond();
                        //get the password from the console.
                        Console console = System.console();
                        String pass = new String(console.readPassword("Enter password:"));
                        create(host, pass, port, user);
                    }
                }
            }
        } else if (Objects.equals(opt.getFirst(), 'a')) {
            opt = op.getLongOpt(false);
            if (Objects.equals(opt.getFirst(), 'u')) {
                user = opt.getSecond();
                opt = op.getLongOpt(false);
                if (Objects.equals(opt.getFirst(), 'h')) {
                    host = opt.getSecond();
                    opt = op.getLongOpt(false);
                    if (Objects.equals(opt.getFirst(), 'p')) {
                        port = opt.getSecond();
                        //get the password from the console.
                        Console console = System.console();
                        String pass = new String(console.readPassword("Enter password:"));
                        //get the otp from the console.
                        System.out.print("Enter OTP: ");
                        Scanner in = new Scanner(System.in);
                        int otp = in.nextInt();
                        auth(host, pass, port, user, otp);
                    }
                }
            }
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
    private static void create(String host, String pass, String port, String user) throws IOException, NoSuchMethodException {
        EnrollRequest send = new EnrollRequest(user, pass);
        SSLSocket out = Communication.connectAndSend(host, Integer.parseInt(port), send);
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
    private static void auth(String host, String pass, String port, String user, int otp) throws IOException, NoSuchMethodException {
        AuthRequest send = new AuthRequest(user, pass, otp);
        SSLSocket out = Communication.connectAndSend(host, Integer.parseInt(port), send);
        final Packet packet = Communication.read(out);
        ServerResponse ServerResponse_packet = (ServerResponse) packet;
        if (ServerResponse_packet.getStatus()) {
            System.out.println("Authenticated.");
        } else {
            System.out.println(ServerResponse_packet.getPayload());
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
    private static void uploadFile(String host, String pass, String port, String user, String ticketString, String filepath) {
        
    }

}
