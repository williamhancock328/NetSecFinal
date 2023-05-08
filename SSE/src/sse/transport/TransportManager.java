package sse.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import packets.filepack.FileSend;

/**
 * Manager for the construction of files sent over 
 * @author Alex
 */
public class TransportManager {
    
    /**
     * Transport table holds the value of files that are being received
     * 
     * Files are sent as "File Fragments" or the File_Send packet's.
     * Each packet has an index, this is the count of where the packet is in the stack
     * When a packet is received with the [isfinal] attribute set to true, we know it is the last packet in a sequence
     * 
     */     
    private HashMap<String, List<FileSend>> transport_table = new HashMap<>();
    
    /**
     * Default constructor
     */
    public TransportManager() {
        
    }
        
    /**
     * Called when receiving a FileSend packet, adjusts it to the transport_table
     * @param ID
     * @param packet 
     */
    public void received(FileSend packet) {
        
        String ID = packet.getID(); // ID of the document
        
        // Add FileSend to the list
        if(transport_table.containsKey(ID)) {
            transport_table.get(ID).add(packet);
        } 
        // Create a new index in the table
        else {
            // Construct a new list, add the packet, put it in the table
            List<FileSend> append = new ArrayList<>();
            append.add(packet);
            transport_table.put(ID, append);
        }
    }
    
    /**
     * Converts a index in the transport_table into an encoded_file string. 
     * 
     * @param ID Reference to the ID of the Document @ the transport table index.
     * @return encoded_file : String
     */
    public String toEncodedFile(String ID) {
        StringBuilder builder = new StringBuilder();
        
        // Get the table and assure it has data
        List<FileSend> ref = transport_table.get(ID);
        if(ref == null || ref.isEmpty()) return "";
        
        // Sort the collection
        Collections.sort(ref);
        
        // Collect all of the file bits into the builder, in order (0-n).
        for(int i = 0; i < ref.size(); i++) {
            System.out.println("Index of file building: "+ ref.get(i).getIndex());
            builder.append(ref.get(i).getFile_bit());
        }
        
        return builder.toString();
    }
    
    /**
     * Breaks a encoded_file (Base64 of a file [could be encrypted or not]) into multiple
     * FileSend packets if necessary
     * @return 
     */
    public List<FileSend> fromEncodedFile(String ID, String encoded_file) {
        final int encoded_fragment_length = 1000; // Each fragment should consist of no more than 1000 bytes.
        
        // Loop through {@code encoded_file} every encoded_fragment_length until the end of the file is reached.
        // https://www.baeldung.com/java-string-split-every-n-characters
        List<String> results = new ArrayList<>();
        int length = encoded_file.length();
        for (int i = 0; i < length; i += encoded_fragment_length) {
            results.add(encoded_file.substring(i, Math.min(length, i + encoded_fragment_length)));
        }

        // Loop through each fragment in results and add it too ret        
        List<FileSend> ret = new ArrayList<>();
        for(int i = 0; i < results.size(); i++) {
            ret.add(new FileSend(ID, results.get(i), i, false));
        }
        
        // Get the last element of ret, and set isfinal to true.
        ret.get(ret.size()-1).setIsfinal(true);
        
        System.out.println( "size: " + ret.size() + " " + (ret.get(ret.size()-1).isIsfinal()) );
        
        return ret;
    }
    
    /**
     * Removes the list of file send objects that are represented by {@code ID}
     * @param ID The Unique Identifier (Primary Key) of a document.
     * @return 
     */
    public List<FileSend> removeIndicies(String ID) {
        return transport_table.remove(ID);
    }
    
    /**
     * Retrieves a FileSend with the associating index
     * @param list List<FileSend> list
     * @param index index value to be received
     * @return a FileSend if one is found with FileSend.index == {@code index}, or else null
     */
    public FileSend getFileSendWithIndex(List<FileSend> list, int index) {
        return list.stream().filter(n -> n.getIndex() == index).findFirst().orElse(null);
    }
    
}
