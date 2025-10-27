package Hotel.jwt.service;
import java.util.Optional;

public interface PasswordResetService {
    void generateAndSendToken(String email);
    boolean resetPassword(String token, String newPassword);


}