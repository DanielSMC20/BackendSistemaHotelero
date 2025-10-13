package Hotel.jwt.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Entity @Table(name="usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="usuario", nullable=false, unique=true, length=80)
    private String usuario;

    @Column(name="password", nullable=false)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable=false, length=20)
    private Rol role;

    @Column(name="nombres")   private String nombres;
    @Column(name="apellidos") private String apellidos;
    @Column(name="pais")      private String pais;



}