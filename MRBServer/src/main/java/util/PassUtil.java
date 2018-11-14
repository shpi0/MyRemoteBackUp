package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PassUtil {
    private static PassUtil ourInstance = new PassUtil();

    public static PassUtil getInstance() {
        return ourInstance;
    }

    private PassUtil() {
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = null;
        digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public String getPassHash(String pass) throws NoSuchAlgorithmException {
        return hashPassword(pass);
    }

}
