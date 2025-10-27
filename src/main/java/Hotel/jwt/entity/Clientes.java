package Hotel.jwt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "clientes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_clientes_documento", columnNames = {"tipo_documento", "documento"}),
                @UniqueConstraint(name = "uk_clientes_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_clientes_tel_compuesto", columnNames = {"telefono_codigo_pais", "telefono"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clientes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Identificación general ===
    @Column(name = "tipo_persona", nullable = false, length = 15)
    private String tipoPersona; // NATURAL o JURIDICA

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento; // DNI, CE, PASAPORTE, RUC10, RUC20

    @Column(name = "documento", nullable = false, length = 20)
    private String documento;

    // === Datos personales o empresariales ===
    @Column(name = "nombres_completos", length = 120)
    private String nombresCompletos; // NATURAL

    @Column(name = "razon_social", length = 150)
    private String razonSocial; // JURIDICA

    @Column(name = "nacionalidad", length = 50)
    private String nacionalidad;

    // === Contacto ===
    @Column(name = "email", length = 80)
    private String email;

    @Column(name = "telefono_codigo_pais", length = 6)
    private String telefonoCodigoPais;      // ej. +51

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "telefono_fijo", length = 20)
    private String telefonoFijo;


    @Column(name = "telefono_e164", length = 20, unique = true)
    private String telefonoE164;

    // === Estado del cliente ===
    @Column(name = "estado", nullable = false, length = 15)
    private String estado = "ACTIVO"; // ACTIVO / INACTIVO

    // === Auditoría básica ===
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) fechaRegistro = java.time.LocalDateTime.now();
        if (estado == null) estado = "ACTIVO";
        if (tipoPersona == null) tipoPersona = "NATURAL";
    }
}
