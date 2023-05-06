
package sse;

import java.util.List;
import java.util.UUID;

/**
 * Represents a document that has been encrypted and stored on the server
 * @author William Hancock
 */
public class EncryptedDocument {
    
   private String ID; 
   private String encoded_file;
   private List<String> users; // List of users who have access too the file (usernames)
   
   /**
    * Construction constructor
    * @param encoded_file 
    */
   public EncryptedDocument(String encoded_file, List<String> users) {
       this.ID = UUID.randomUUID().toString();
       this.encoded_file = encoded_file;
   }
   
   /**
    * Loading constructor
    * 
    * @param ID
    * @param encoded_file
    * @param users 
    */
   public EncryptedDocument(String ID, String encoded_file, List<String> users) {
       this.ID = ID;
       this.encoded_file = encoded_file;
       this.users = users;
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

}
