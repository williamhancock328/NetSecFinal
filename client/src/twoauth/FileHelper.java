package twoauth;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 *
 * @author Alex
 */
public class FileHelper {
    
    /**
     * Encodes a File to a string using Base64
     * 
     * Reference
     * https://stackoverflow.com/questions/37066216/java-encode-file-to-base64-string-to-match-with-other-encoded-string
     * @param fileName
     * @return
     * @throws IOException 
     */
    public static String encodeFileToBase64String(String fileName) throws IOException {
        File file = new File(fileName);
        String encoded = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
        return encoded;
    }

    /**
     * Decodes a base64 encoded string to a file in destination {@code path}
     * 
     * @param base64Encoded
     * @param fileName
     * @param path
     * @throws IOException 
     */
    public static void decodeFileStringToFile(String base64Encoded, String fileName, String path) throws IOException {
        byte[] decodedImg = Base64.getDecoder().decode(base64Encoded.getBytes(StandardCharsets.UTF_8));
        Path destinationFile = Paths.get(path, fileName);
        Files.write(destinationFile, decodedImg);
    }
    
    
}
