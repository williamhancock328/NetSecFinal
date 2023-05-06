
package sse;

import java.util.UUID;

/**
 * Represents a document that has been encrypted and stored on the server
 * @author William Hancock
 */
public class EncryptedDocument {
    
   private String ID; 
   private String encoded_file;
   
   /**
    * Main constructor
    * @param encoded_file 
    */
   public EncryptedDocument(String encoded_file) {
       this.ID = UUID.randomUUID().toString();
       this.encoded_file = encoded_file;
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

   
   

}
