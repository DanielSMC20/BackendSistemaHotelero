package Hotel.jwt.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    // Clave de ejemplo. Muévelo a variable de entorno.
    private final Key key = Keys.hmacShaKeyFor("clave-super-secreta-para-hoteles-32bytes!!".getBytes());

    public String generate(String username, Map<String, Object> claims, long millis) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + millis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        try {
            return extractUsername(token).equals(username)
                    && Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody().getExpiration().after(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    public UsernamePasswordAuthenticationToken buildAuthentication(String username, UserDetailsService uds) {
        var user = uds.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
