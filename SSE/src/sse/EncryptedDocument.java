
package sse;

import java.util.List;
import java.util.UUID;

/**
 * Represents a document that has been encrypted and stored on the server
 * @author William Hancock
 */
public class EncryptedDocument {
    
   private String ID;
   private String encrypted_filename;
   private String encoded_file = "";
   private List<String> users; // List of users who have access too the file (usernames)
   
   /**
    * Construction constructor
    * @param encoded_file 
    */
   public EncryptedDocument(String encrypted_filename, List<String> users) {
       this.ID = UUID.randomUUID().toString();
       this.encrypted_filename = encrypted_filename;
       this.users = users;
   }
   
   /**
    * Loading constructor
    * 
    * @param ID
    * @param encoded_file
    * @param users 
    */
   public EncryptedDocument(String ID, String encrypted_filename, String encoded_file, List<String> users) {
       this.ID = ID;
       this.encoded_file = encoded_file;
       this.users = users;
       this.encrypted_filename = encrypted_filename;
   }


   /**
    * Determines if this object is equal to EncryptedDocument
    * @param obj
    * @return 
    */
   @Override
   public boolean equals(Object obj) {
        if(!(obj instanceof EncryptedDocument )) 
            return false;
        EncryptedDocument other = (EncryptedDocument) obj;
        
        // Return if the comparisons are equal
        return UUID.fromString(getID()).equals(UUID.fromString(other.getID()));
    }

    /**
     * @return the ID
     */
    public String getID() {
        return ID;
    }

    /**
     * @return the encoded_file
     */
    public String getEncoded_file() {
        return encoded_file;
    }

    /**
     * @return the users
     */
    public List<String> getUsers() {
        return users;
    }

    /**
     * @param encoded_file the encoded_file to set
     */
    public void setEncoded_file(String encoded_file) {
        this.encoded_file = encoded_file;
    }

    /**
     * @return the encoded_filename
     */
    public String getEncrypted_filename() {
        return encrypted_filename;
    }

}
