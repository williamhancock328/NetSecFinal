package json;

import merrimackutil.json.types.JSONObject;

/**
 * Represents an user block as a JSONObject.
 */
public class PwValue {

    private String salt;
    private String pass;
    private String totpkey;
    private String user;

    /**
     * Constructs a new PwValue object.
     * @param salt
     * @param pass
     * @param totpkey
     * @param user 
     */
    public PwValue(String salt, String pass, String totpkey, String user) {
        this.salt = salt;
        this.pass = pass;
        this.totpkey = totpkey;
        this.user = user;
    }

    /**
     * Constructs a new JSONObject from the PwValue object.
     * @param obj 
     */
    public JSONObject getJson() {
        JSONObject obj = new JSONObject();
        obj.put("salt", salt);
        obj.put("pass", pass);
        obj.put("totp-key", totpkey);
        obj.put("user", user);
        return obj;
    }

    public String getSalt() {
        return salt;
    }

    public String getPass() {
        return pass;
    }

    public String getTotpkey() {
        return totpkey;
    }

    public String getUser() {
        return user;
    }

    
    
}
