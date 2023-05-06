package sse.client;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import sse.Token;

/**
 * Takes a list of keywords and converts it into a list of searchable tokens
 * @author Alex
 */
public class Tokenizer {
 
    /**
     * Protocol:
     * 
     * Tokens are constructed from a list of keywords that are encrypted using the session key.
     */
    
    /**
     * Converts a list of plain-text keywords into Tokens from the derived hash of the session_key.
     * 
     * 
     * @param keywords List of keywords associated with a document 
     * @param session_key Key used to encrypt the tokens
     * @return 
     */
    public static List<Token> tokenize(List<String> keywords, Key key) {
        List<Token> ret = new ArrayList<>();
        
        
        
        return ret;
    }
    
}
