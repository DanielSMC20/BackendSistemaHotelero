package Hotel.jwt.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String token);
}
