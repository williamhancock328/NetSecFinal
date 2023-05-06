package cloudservice.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 *
 * @author Alex
 */
public class Config implements JSONSerializable {
    
    private String path;
    
    private int port;
    private boolean debug;
    private String service_name;
    private String service_secret;

    
    public Config(String path) throws FileNotFoundException, InvalidObjectException {
        this.path = path;
        
        // Construct file
        File file = new File(path);
        
        if(file == null || !file.exists()) {
            throw new FileNotFoundException("File from path for EchoServiceConfig does not point to a vadlid configuration json file.");
        }
        
        // Construct JSON Object and load configuration
        JSONObject obj = JsonIO.readObject(file);
        deserialize(obj);
    }

    @Override
    public String serialize() {
        return toJSONType().getFormattedJSON();// We should never be converting this file to JSON, only read.
    }

    @Override
    public void deserialize(JSONType type) throws InvalidObjectException {
        
        JSONObject obj;
        if(type instanceof JSONObject) {
            obj = (JSONObject) type;
        } else { throw new InvalidObjectException("Expected EchoServiceConfig Type - JsonObject. "); }
        
        if(obj.containsKey("service-name")) {
            this.service_name = obj.getString("service-name");
        } else { throw new InvalidObjectException("Expected an EchoServiceConfig object -- service_name-file expected."); }
        
        if(obj.containsKey("port")) {
            this.port = obj.getInt("port");
        } else { throw new InvalidObjectException("Expected an EchoServiceConfig object -- port expected."); }
        
        if(obj.containsKey("debug")) {
            this.debug = obj.getBoolean("debug");
        } else { throw new InvalidObjectException("Expected an EchoServiceConfig object -- debug expected."); }
        
        if(obj.containsKey("service-secret")) {
            this.service_secret = obj.getString("service-secret");
        } else { throw new InvalidObjectException("Expected an EchoServiceConfig object -- service_secret expected."); }
                    
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("port", this.port);
        obj.put("debug", this.debug);
        obj.put("service-name", this.service_name);
        obj.put("service-secret", this.service_secret);
        return obj; // We are never reading this file to JSON.
    }

    /**
     * Accessors
     */
    
    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @return the service_name
     */
    public String getService_name() {
        return service_name;
    }

    /**
     * @return the service_secret
     */
    public String getService_secret() {
        return service_secret;
    }

}
