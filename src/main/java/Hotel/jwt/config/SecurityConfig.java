package Hotel.jwt.config;
import Hotel.jwt.security.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:4200"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/external/**").permitAll()

                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers("/users/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/rooms/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")
                        .requestMatchers(HttpMethod.POST, "/rooms/**").hasAnyRole("ADMIN","RECEPCIONISTA")
                        .requestMatchers(HttpMethod.PUT, "/rooms/**").hasAnyRole("ADMIN","RECEPCIONISTA")
                        .requestMatchers(HttpMethod.PATCH, "/rooms/**").hasAnyRole("ADMIN","RECEPCIONISTA")
                        .requestMatchers(HttpMethod.DELETE, "/rooms/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/customers/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")
                        .requestMatchers(HttpMethod.GET, "/customers/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")

                        .requestMatchers(HttpMethod.POST, "/reservations/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")
                        .requestMatchers(HttpMethod.GET, "/reservations/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")

                        .requestMatchers(HttpMethod.POST, "/payments/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")
                        .requestMatchers(HttpMethod.GET, "/payments/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")

                        .requestMatchers(HttpMethod.POST, "/invoices/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")
                        .requestMatchers(HttpMethod.GET, "/invoices/**").hasAnyRole("ADMIN","GERENTE","RECEPCIONISTA")

                        .requestMatchers("/reports/**").hasAnyRole("ADMIN","GERENTE")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


}