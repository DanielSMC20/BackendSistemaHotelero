package Hotel.jwt.auth.impl;
import Hotel.jwt.auth.AuthService;
import Hotel.jwt.dto.auth.AuthResponse;
import Hotel.jwt.dto.auth.RegisterRequest;
import Hotel.jwt.entity.Usuario;
import Hotel.jwt.repository.UsuarioRepository;
import Hotel.jwt.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest r) {

        var user = Usuario.builder()
                .usuario(r.getUsuario())
                .clave(encoder.encode(r.getClave()))
                .nombres(r.getNombres())
                .apellidos(r.getApellidos())
                .pais(r.getPais())
                .role(r.getRol()) // ✅ el campo correcto en Usuario es 'rol'
                .build();

        usuarioRepo.save(user);

        var token = jwtService.generate(
                r.getUsuario(),
                Map.of("app", "hotel-crm"),
                1000L * 60 * 60 * 6 // 6 horas
        );

        return new AuthResponse(token);
    }
}