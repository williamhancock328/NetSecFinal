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
     * The client for the file sharing system.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, NoSuchMethodException {
        // Parse the command line arguments.
        OptionParser op = new OptionParser(args);
        LongOption[] ar = new LongOption[7];
        ar[0] = new LongOption("create", false, 'c');
        ar[1] = new LongOption("add", false, 'a');
        ar[2] = new LongOption("download", false, 'd');
        ar[3] = new LongOption("user", true, 'u');
        ar[4] = new LongOption("host", true, 'h');
        ar[5] = new LongOption("port", true, 'p');
        ar[6] = new LongOption("file", true, 'f');
        op.setLongOpts(ar);
        op.setOptString("cadu:h:p:f:");
        Tuple<Character, String> opt = op.getLongOpt(false);
        if (opt == null) {
            System.out.println("usage:\n"
                    + "client --create --user <user> --host <host> --port <portnum>\n"
                    + "client --add --user <user> --host <host> --port <portnum> --file <filename>\n"
                    + "client --download --user <user> --host <host> --port <portnum>\n"
                    + "options:\n"
                    + "-c, --create Create a new account.\n"
                    + "-a, --add Add a file.\n"
                    + "-d, --download Download a file.\n"
                    + "-u, --user The user name.\n"
                    + "-h, --host The host name of the server.\n"
                    + "-p, --port The port number for the server."
                    + "-f  --file The name of the file to upload.");
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
                        System.out.println("adduser");
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
                        opt = op.getLongOpt(false);
                        if (Objects.equals(opt.getFirst(), 'f')) {
                            System.out.println("adding a file. ");
                            //get the password from the console.
                            Console console = System.console();
                            String pass = new String(console.readPassword("Enter password:"));
                            //get the otp from the console.
                            System.out.print("Enter OTP: ");
                            Scanner in = new Scanner(System.in);
                            int otp;
                            while (!in.hasNextInt()) {
                                System.out.println("Authentication failed.");
                                System.exit(0);
                            }
                            otp = in.nextInt();
                            if (auth(host, pass, port, user, otp)) {

                            } else {
                                System.out.println("Authentication failed.");
                                System.exit(0);
                            }
                        }
                    }
                }
            }
        } else if (Objects.equals(opt.getFirst(), 'd')) {
            opt = op.getLongOpt(false);
            if (Objects.equals(opt.getFirst(), 'u')) {
                user = opt.getSecond();
                opt = op.getLongOpt(false);
                if (Objects.equals(opt.getFirst(), 'h')) {
                    host = opt.getSecond();
                    opt = op.getLongOpt(false);
                    if (Objects.equals(opt.getFirst(), 'p')) {
                        port = opt.getSecond();
                        opt = op.getLongOpt(false);
                        if (Objects.equals(opt.getFirst(), 'f')) {
                            System.out.println("dl a file. ");
                            //get the password from the console.
                            Console console = System.console();
                            String pass = new String(console.readPassword("Enter password:"));
                            //get the otp from the console.
                            System.out.print("Enter OTP: ");
                            Scanner in = new Scanner(System.in);
                            int otp = in.nextInt();
                            if (auth(host, pass, port, user, otp)) {

                            } else {
                                System.out.println("Authentication failed.");
                                System.exit(0);
                            }
                        }
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
    private static boolean auth(String host, String pass, String port, String user, int otp) throws IOException, NoSuchMethodException {
        AuthRequest send = new AuthRequest(user, pass, otp);
        SSLSocket out = Communication.connectAndSend(host, Integer.parseInt(port), send);
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
    private static void uploadFile(String host, String pass, String port, String user, String ticketString, String filepath) {

    }

}
