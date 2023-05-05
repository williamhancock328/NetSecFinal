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

    private static String host;
    private static String port;

    /**
     * The client for the file sharing system.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, NoSuchMethodException {
        //print a welcome message then a menu with the options to create a user, download a file, upload a file, manage tags, and search for files by tag
        System.out.println("Welcome to the File Sharing System!");
        System.out.println("Please enter the host you would like to connect to: ");
        Console console = System.console();
        Scanner scanner = new Scanner(System.in);
        host = scanner.nextLine();
        System.out.println("Please enter the port you would like to connect to: ");
        port = scanner.nextLine();
        System.out.println("Login Menu:");
        System.out.println("1. Create a new user");
        System.out.println("2. Login as an existing user");
        System.out.println("3. Exit");
        int input = scanner.nextInt();
        switch(input){
            case 1:
                System.out.println("Please enter the username you would like to use: ");
                String newuser = scanner.nextLine();
                String newpass = new String(console.readPassword("Enter password:"));
                create(host, newpass, port, newuser);
                break;
            case 2:
                System.out.println("Please enter your username: ");
                String user = scanner.nextLine();
                String pass = new String(console.readPassword("Enter password:"));
                System.out.println("Please enter your one time password: ");
                int otp = scanner.nextInt();
                if(auth(user, pass, pass, user, otp)){
                    System.out.println("Login successful!");
                    System.out.println("Main Menu:");
                    System.out.println("1. Upload a file");
                    System.out.println("2. Download a file");
                    System.out.println("3. Manage tags");
                    System.out.println("4. Search for files by tag");
                    System.out.println("5. Exit");
                    int input2 = scanner.nextInt();
                    switch(input2){
                        case 1:
                            System.out.println("Please enter the filepath of the file you would like to upload: ");
                            String filepath = scanner.nextLine();
                            System.out.println("Please enter the tags you would like to use for this file: ");
                            String tags = scanner.nextLine();
                            uploadFile(host, pass, port, user, null, tags.split(" "), filepath);
                            break;
                        case 2:
                            System.out.println("Please enter the name of the file you would like to download: ");
                            String filename = scanner.nextLine();
                            System.out.println("Please enter the filepath you would like to download the file to: ");
                            String filepath2 = scanner.nextLine();
                            downloadFile(host, pass, port, user, null, filename, filepath2);
                            break;
                        case 3:
                            System.out.println("Please enter the name of the file you would like to manage tags for: ");
                            String filename2 = scanner.nextLine();
                            System.out.println("Please enter the tags you would like to use for this file: ");
                            String tags2 = scanner.nextLine();
                            manageTags(host, pass, port, user, null, filename2, tags2.split(" "));
                            break;
                        case 4:
                            System.out.println("Please enter the tags you would like to search for: ");
                            String tags3 = scanner.nextLine();
                            searchTags(host, pass, port, user, null, tags3.split(" "));
                            break;
                        case 5:
                            System.exit(0);
                            break;
                        default:
                            System.out.println("Invalid input.");
                            break;
                    }
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
    private static void uploadFile(String host, String pass, String port, String user, String ticketString, String[] tags, String filepath) {

    }

    
}
