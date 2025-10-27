package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.entity.PasswordResetToken;
import Hotel.jwt.repository.PasswordResetTokenRepository;
import Hotel.jwt.repository.UsuarioRepository;
import Hotel.jwt.service.EmailService;
import Hotel.jwt.service.PasswordResetService;
import Hotel.jwt.entity.Usuario;
import Hotel.jwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository usuarioRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public void generateAndSendToken(String email) {
        var user = usuarioRepo.findByUsuario(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Correo no registrado"));
        tokenRepo.deleteByEmail(email); // solo un token activo por email

        String token = UUID.randomUUID().toString();
        var entity = PasswordResetToken.builder()
                .email(email)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
        tokenRepo.save(entity);

        emailService.sendPasswordResetEmail(email, token);
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        var prtOpt = tokenRepo.findByTokenAndExpiresAtAfterAndUsedAtIsNull(token, LocalDateTime.now());
        if (prtOpt.isEmpty()) return false;

        var prt = prtOpt.get();
        var user = usuarioRepo.findByUsuario(prt.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        user.setClave(passwordEncoder.encode(newPassword));
        usuarioRepo.save(user);

        prt.setUsedAt(LocalDateTime.now());
        tokenRepo.save(prt);

        return true;
    }
}