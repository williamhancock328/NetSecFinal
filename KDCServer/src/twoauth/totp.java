package twoauth;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import json.Vault;

/**
 * Class that implements time based onetime password (TOTP).
 * @author William Hancock
 */
public class totp {

    String k;
    int otp;

    /**
     * Constructs a new TOTP object.
     *
     * @param k The secret key.
     * @param otp The OTP.
     */
    public totp(String k, int otp) {
        this.k = k;
        this.otp = otp;
    }

    /**
     * Checks the Time-Based One-Time Password.
     *
     * @return true if the OTP is correct, false otherwise.
     */
    public boolean CheckOtp() {
        int bin_code = 0;
        try {
            //Generate a HMAC of the counter value.
            byte[] hmac_result = hash(k, calcV());
            //Extract the one time password from the HMAC as described in RFC 4226.
            int offset = hmac_result[19] & 0xf;
            bin_code = (hmac_result[offset] & 0x7f) << 24
                    | (hmac_result[offset + 1] & 0xff) << 16
                    | (hmac_result[offset + 2] & 0xff) << 8
                    | (hmac_result[offset + 3] & 0xff);
            //Truncate the one time password to 6 digits.
            bin_code = bin_code % (int)Math.pow(10, 6);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            return false;
        }
        //Compare the one time password to the user's OTP.
        if (bin_code == otp) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * This method generates a HMAC of the counter value.
     * @param key The secret key.
     * @param time The counter value.
     * @return The HMAC of the counter value.
     * @throws NoSuchAlgorithmException Thrown if the algorithm HmacSHA1 is not found.
     * @throws InvalidKeyException Thrown if the key is invalid.
     * 
     */
    private byte[] hash(String key, byte[] time) throws NoSuchAlgorithmException, InvalidKeyException {
        //Generate a HMAC of the counter value.
        byte[] decKey = Base64.getDecoder().decode(key);
        SecretKeySpec macKey = new SecretKeySpec(decKey, "HmacSHA1");
        Mac hmac = Mac.getInstance("HmacSHA1");
        hmac.init(macKey);
        byte[] tag = hmac.doFinal(time);
        return tag;
    }

    /*
     * This method generates the counter value.
     * @return The counter value.
     */
    private byte[] calcV() {
        long currTime = Instant.now().getEpochSecond();
        long v = Math.floorDiv((currTime - (long) 0), (long) 30);
        return longToBytes(v);
    }

    /**
     * This method converts a long value into an 8 - byte value .
     *
     * @param num the number to convert to bytes .
     * @return an array of 8 bytes representing the number num.
     */
    private byte[] longToBytes(long num) {
        byte[] res = new byte[8];

        // Decompose the a long type into byte components.
        for (int i = 7; i >= 0; i--) {
            res[i] = (byte) (num & 0xFF);
            num >>= 8;
        }
        return res;
    }
}
