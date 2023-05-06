package json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;

/**
 * Represents a vault as a JSONObject.
 *
 * @author William Hancock
 */
public class Vault {

    private static JSONObject vault;
    private static String FILE_NAME;

    /**
     * Constructor for Vault
     */
    public Vault(String fname) throws IOException {
        FILE_NAME = fname;
        init();
    }

    /**
     * Initializes the vault
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void init() throws FileNotFoundException, IOException {
        // Initialize vault
        vault = new JSONObject();

        // Determine if file exists
        File file = new File(FILE_NAME);

        // If not, create file
        if (!file.exists() || !file.isFile()) {
            file.createNewFile();
        } // Else load file
        else {
            LoadVault(file);
        }
    }

/**
     * loads the vault from a file
     * 
     * @param f the file to load from
     * @throws FileNotFoundException
     */
    private void LoadVault(File f) throws FileNotFoundException {
        // Read JSON into vault
        try {
            vault = JsonIO.readObject(f);
        } catch (NullPointerException e) {
            System.out.println("Vault Empty");
        }
    }
    
    /**
     * Adds a user's entry to the vault
     *
     * @param salt the salt for the user
     * @param pass the password for the user
     * @param totpkey the totp key for the user
     * @param user the user to add
     */
    public void AddPW(String salt, String pass, String totpkey, String user) {
        PwValue tmp = new PwValue(salt, pass, totpkey, user);
        JSONObject toAdd = tmp.getJson();
        //initialize the account array if it is empty 
        if (vault.getArray("entries") == null) {
            JSONArray arr = new JSONArray();
            arr.add(toAdd);
            vault.put("entries", arr);
        } else {
            vault.getArray("entries").add(toAdd);
        }
    }

    /**
     * Gets a user's entry from the vault
     *
     * @param user the user to search for
     * @return the user's entry
     */
    public PwValue GetPW(String user) {
        if (vault.getArray("entries") == null) {
            return null;
        } else {
            JSONArray accts = vault.getArray("entries");
            //look for the URL in the array
            for (Object ent : accts) {
                if (((JSONObject) ent).getString("user").equals(user)) {
                    String salt = ((JSONObject) ent).getString("salt");
                    String pass = ((JSONObject) ent).getString("pass");
                    String totpkey = ((JSONObject) ent).getString("totp-key");
                    String u = ((JSONObject) ent).getString("user");
                    return new PwValue(salt, pass, totpkey, u);
                }
            }
        }
        return null;
    }

    /**
     * Saves the vault to a file
     */
    public void SaveJSON() {
        //initialize vault file
        File file = new File(FILE_NAME);
        try {
            if (!file.exists() || !file.isFile()) {
                file.createNewFile();
            }

            // Write formatted json to FILE
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(vault.getFormattedJSON());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
