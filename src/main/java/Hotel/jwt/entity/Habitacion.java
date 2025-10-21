package Hotel.jwt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "habitaciones",
        uniqueConstraints = @UniqueConstraint(name = "uk_habitacion_codigo", columnNames = "codigo"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Código autogenerado: HB-001, HB-002... ===
    @Column(name = "codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    // === Número visible o interno del hotel ===
    @Column(name = "numero", nullable = false, unique = true, length = 10)
    private String numero;

    // === Tipo de habitación (enum o entidad externa) ===
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoHabitacion tipo;

    // === Capacidad máxima de personas ===
    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    // === Número de camas ===
    @Column(name = "camas", nullable = false)
    private Integer camas;

    // === Rango de habitación (económica, estándar, suite, lujo...) ===
    @Column(name = "rango", length = 30)
    private String rango;

    // === Estados: DISPONIBLE / OCUPADA / MANTENIMIENTO ===
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    // === Precio por noche y por hora ===
    @Column(name = "precio_por_noche", nullable = false)
    private Double precioPorNoche;

    @Column(name = "precio_por_hora", nullable = false)
    private Double precioPorHora;

    // === Descripción / Detalles adicionales ===
    @Column(name = "detalles", length = 200)
    private String detalles;

    // === Auditoría simple ===
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (estado == null) estado = "DISPONIBLE";
        // NO generes 'codigo' aquí. El servicio lo asigna con HB-###
    }


    private String generarCodigoHabitacion() {
        // Código local (no consulta BD). En el servicio se reemplaza por uno real.
        String rand = java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "HB-" + rand;
    }

    // --- Compatibilidad con el front legacy que usa "disponible"
    @Transient
    public Boolean getDisponible() {
        if (estado == null) return Boolean.TRUE; // por defecto disponible
        return "DISPONIBLE".equalsIgnoreCase(estado);
    }

    public void setDisponible(Boolean disponible) {
        if (disponible == null) return;
        this.estado = disponible ? "DISPONIBLE" : "OCUPADA";
    }

}
