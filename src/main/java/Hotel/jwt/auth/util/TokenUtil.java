package Hotel.jwt.auth.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class TokenUtil {
    private final SecureRandom rnd = new SecureRandom();

    public String randomBase64Url(int bytes) {
        byte[] b = new byte[bytes];
        rnd.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    public String sha256Hex(String s) {
        try {
            var dig = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(dig.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("No SHA-256", e);
        }
    }
}