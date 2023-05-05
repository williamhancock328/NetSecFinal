package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Class that loads the configuration file for the authentication server.
 * @author William
 */
public class SSLConfig implements JSONSerializable {
    
    private String path;
    
    private String password_file;
    private int port;
    private String keystore_file;
    private String keystore_pass;
    
    /**
     * Constructs a new Config object from a path to a configuration JSON file.
     * @param path the path to the configuration file.
     * @throws FileNotFoundException
     * @throws InvalidObjectException 
     */
    public SSLConfig(String path) throws FileNotFoundException, InvalidObjectException {
        this.path = path;
        
        // Construct file
        File file = new File(path);
        
        if(file == null || !file.exists()) {
            throw new FileNotFoundException("File from path for Config does not point to a vadlid configuration json file.");
        }
        
        // Construct JSON Object and load configuration
        JSONObject obj = JsonIO.readObject(file);
        deserialize(obj);
    }

    @Override
    public String serialize() {
        return toJSONType().getFormattedJSON();
    }

    @Override
    public void deserialize(JSONType type) throws InvalidObjectException {
        
        JSONObject obj;
        if(type instanceof JSONObject) {
            obj = (JSONObject) type;
        } else { throw new InvalidObjectException("Expected Config Type - JsonObject. "); }
        
        if(obj.containsKey("password-file")) {
            this.password_file = obj.getString("password-file");
        } else { throw new InvalidObjectException("Expected an Config object -- password-file expected."); }
        
        if(obj.containsKey("port")) {
            this.port = obj.getInt("port");
        } else { throw new InvalidObjectException("Expected an Config object -- port expected."); }
        
        if(obj.containsKey("keystore-file")) {
            this.keystore_file = obj.getString("keystore-file");
        } else { throw new InvalidObjectException("Expected an Config object -- keystore-file expected."); }
        
        if(obj.containsKey("keystore-pass")) {
            this.keystore_pass = obj.getString("keystore-pass");
        } else { throw new InvalidObjectException("Expected an Config object -- keystore-pass expected."); }
                    
    }

   @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("password-file", this.password_file);
        obj.put("port", this.port);
        obj.put("keystore-file", this.keystore_file);
        obj.put("keystore-pass", this.keystore_pass);
        return obj;
    }

    public String getPassword_file() {
        return password_file;
    }

    public int getPort() {
        return port;
    }

    public String getKeystore_file() {
        return keystore_file;
    }

    public String getKeystore_pass() {
        return keystore_pass;
    }

}
