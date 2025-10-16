package Hotel.jwt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Column(name = "monto", nullable = false)
    private Double monto;

    @Column(name = "metodo", nullable = false, length = 20) // EFECTIVO, TARJETA, TRANSFERENCIA, etc.
    private String metodo;

    @Column(name = "pagado_en", nullable = false)
    private LocalDateTime pagadoEn;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado; // EJ: PENDIENTE, COMPLETADO, REEMBOLSADO

    @Column(name = "referencia", length = 100)
    private String referencia;

    @Column(name = "registrado_por", length = 50)
    private String registradoPor;

    @PrePersist
    void alPagar() {
        this.pagadoEn = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = "COMPLETADO";
        }
    }
}

