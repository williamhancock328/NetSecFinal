package sse.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Represents the database that holds values of EncryptedDocuments
 * @author Alex
 */
public class Database implements JSONSerializable {
    
    private String path;
    
    public Database(String path) throws FileNotFoundException, InvalidObjectException, IOException {
        this.path = path;
        
        // Construct file
        File file = new File(path);
        
        if(file == null || !file.exists()) {
            //throw new FileNotFoundException("File from path for EchoServiceConfig does not point to a vadlid configuration json file.");
            file.createNewFile(); // Construct a new file and add nothing to the documents database
            update();
            return;
        }
        
        // Construct JSON Object and load entries
        JSONObject obj = JsonIO.readObject(file);
        JSONArray entries = obj.getArray("entries");
        // deserialize
        deserialize(entries);
    }

    @Override
    public String serialize() {
        return toJSONType().getFormattedJSON();// We should never be converting this file to JSON, only read.
    }

    @Override
    public void deserialize(JSONType type) throws InvalidObjectException {
        if(type instanceof JSONArray) {
            JSONArray array = (JSONArray) type;
            
            // Construct a list of entries
            List<Entry> entries = array.stream()
                    .filter(n -> n instanceof JSONObject)
                    .map(n -> (JSONObject)n)
                    .map(n -> {
                        try {
                            return new Entry(n);
                        } catch(InvalidObjectException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            sse.SSE.getDocCollection().fromDatabase(entries);
        }       
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        
        ArrayList<JSONType> secretList = new ArrayList<>();
        List<Entry> entry_list = sse.SSE.getDocCollection().toEntries();

        for (Entry s : entry_list)
        {
            secretList.add(s.toJSONType());
        }

        System.out.println("Entry List Size: " + entry_list.size());
       
        obj.put("entries", new JSONArray(secretList)); // Assign the entries array.
        return obj; // We are never reading this file to JSON.
    }
    
    /**
     * Updates this database
     */
    public void update() {
        try
        {      
         BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
         writer.write(serialize());
         writer.close();
        }
        catch (FileNotFoundException ex)
        {
          System.out.println("Could not save vault to disk.");
          System.out.println(ex);
        } catch (IOException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}