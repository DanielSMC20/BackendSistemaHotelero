package Hotel.jwt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario", nullable = false, unique = true, length = 80)
    private String usuario;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Rol role;

    @Column(name = "nombres", length = 100)
    private String nombres;

    @Column(name = "apellidos", length = 100)
    private String apellidos;

    @Column(name = "pais", length = 50)
    private String pais;

    @Column(name = "estado", nullable = false)
    private Integer estado;   // 1 = activo, 0 = inactivo

    // ==============================================
    // Métodos para Spring Security (UserDetails)
    // ==============================================
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return clave;
    }

    @Override
    public String getUsername() {
        return usuario;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        // Solo está habilitado si estado = 1
        return estado != null && estado == 1;
    }

    // Helper opcional para mostrar nombre completo
    public String getNombreCompleto() {
        String n = (nombres != null ? nombres.trim() : "");
        String a = (apellidos != null ? apellidos.trim() : "");
        return (n + " " + a).trim();
    }
}