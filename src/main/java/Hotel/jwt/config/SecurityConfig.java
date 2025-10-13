package Hotel.jwt.config;
import Hotel.jwt.security.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity // por si usas @PreAuthorize en controladores
public class SecurityConfig {

    private final JwtAuthFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(c -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Público
                        .requestMatchers("/auth/**").permitAll()

                        // Usuarios del sistema (solo ADMIN)
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        // Habitaciones
                        .requestMatchers(HttpMethod.GET, "/rooms/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")
                        .requestMatchers(HttpMethod.POST, "/rooms/**").hasAnyRole("ADMIN","GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/rooms/**").hasAnyRole("ADMIN","GERENTE")
                        .requestMatchers(HttpMethod.PATCH, "/rooms/**").hasAnyRole("ADMIN","GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/rooms/**").hasRole("ADMIN")

                        // Clientes
                        .requestMatchers(HttpMethod.POST, "/customers/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")
                        .requestMatchers(HttpMethod.GET, "/customers/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")

                        // Reservas
                        .requestMatchers(HttpMethod.POST, "/reservations/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")
                        .requestMatchers(HttpMethod.GET, "/reservations/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")

                        // Pagos
                        .requestMatchers(HttpMethod.POST, "/payments/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")
                        .requestMatchers(HttpMethod.GET, "/payments/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA") // o quita recepcionista si quieres

                        // Facturas
                        .requestMatchers(HttpMethod.POST, "/invoices/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")
                        .requestMatchers(HttpMethod.GET, "/invoices/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")

                        // Reportes (solo gerencia y admin)
                        .requestMatchers("/reports/**").hasAnyRole("ADMIN","GERENTE")

                        // El resto, autenticado
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}