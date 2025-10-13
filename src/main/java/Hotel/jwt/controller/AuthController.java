    package Hotel.jwt.controller;


    import Hotel.jwt.dto.auth.LoginRequest;
    import Hotel.jwt.dto.auth.LoginResponse;
    import Hotel.jwt.dto.auth.RegisterRequest;
    import Hotel.jwt.entity.Usuario;
    import Hotel.jwt.repository.UserRepository;
    import Hotel.jwt.security.jwt.JwtService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.web.bind.annotation.*;

    import java.util.Map;

    @RestController
    @RequestMapping("/auth")
    @RequiredArgsConstructor
    public class AuthController {

        private final AuthenticationManager authManager;
        private final JwtService jwtService;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;


        @PostMapping("/register")
        public LoginResponse register(@RequestBody RegisterRequest req) {

            if (userRepository.existsByUsuario(req.getUsuario())) {
                throw new RuntimeException("El usuario ya existe: " + req.getUsuario());
            }

            Usuario user = Usuario.builder()
                    .usuario(req.getUsuario())
                    .clave(passwordEncoder.encode(req.getClave()))
                    .nombres(req.getNombres())
                    .apellidos(req.getApellidos())
                    .pais(req.getPais())
                    .role(req.getRol())
                    .build();

            userRepository.save(user);

            long duracion = 1000L * 60 * 60 * 6; // 6 horas
            String token = jwtService.generate(req.getUsuario(), Map.of("app", "hotel-crm"), duracion);

            return new LoginResponse(token, "Usuario registrado exitosamente");
        }
        @PostMapping("/login")
        public LoginResponse login(@RequestBody LoginRequest req){
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            long duracion = 1000L * 60 * 60 * 6; // 6 horas
            String token = jwtService.generate(req.getUsername(), Map.of("app","hotel-crm"), duracion);

            return new LoginResponse(token, "Autenticación exitosa");
        }

    }