package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        String subject = "Recuperación de contraseña";
        String resetUrl = "http://localhost:4200/reset-password?token=" + token;

        String body = "Hola,\n\n" +
                "Haz clic en este enlace para restablecer tu contraseña:\n" +
                resetUrl + "\n\n" +
                "Si no fuiste tú, ignora este mensaje.";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject(subject);
        msg.setText(body);
        msg.setFrom("mitmacontreras@gmail.com");

        mailSender.send(msg);
    }
}
