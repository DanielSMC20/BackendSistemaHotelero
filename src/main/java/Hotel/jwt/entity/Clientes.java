package Hotel.jwt.entity;

import jakarta.persistence.*;
import lombok.*;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes", uniqueConstraints = @UniqueConstraint(name = "uk_clientes_documento", columnNames = "documento"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Clientes {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombres_completos", nullable = false, length = 120)
    private String nombresCompletos;

    @Column(name = "email", length = 80, unique = true)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "documento", length = 30, nullable = false, unique = true) // <- IMPORTANTE
    private String documento;
}
