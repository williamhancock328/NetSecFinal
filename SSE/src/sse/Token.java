
package sse;

/**
 * Represents a single immutable Token Object. 
 * @author William Hancock
 */
public class Token {

    private String encrypted_value;
    
    /**
     * Default constructor
     */
    public Token(String encrypted_value) {
        this.encrypted_value = encrypted_value;
    }

    /**
     * @return the value of this token
     */
    public String getValue() {
        return encrypted_value;
    }
    
    /**
    * Determines if two this.Token is equal to an Object {@code obj}
    * @param obj
    * @return 
    */
   @Override
   public boolean equals(Object obj) {
        if(!(obj instanceof Token)) 
            return false;
        Token other = (Token) obj;
        
        // Return if the comparisons are equal
        return getValue().equalsIgnoreCase(other.getValue());
    }
   
   @Override
   public String toString() {
       return getValue();
   }
    
}
