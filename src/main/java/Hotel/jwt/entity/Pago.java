package Hotel.jwt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Relación: cada pago pertenece a una reserva ===
    @ManyToOne(optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    // === Monto total del pago ===
    @Column(name = "monto", nullable = false)
    private Double monto;

    // === Método: EFECTIVO / TARJETA / TRANSFERENCIA / YAPE / PLIN
    @Column(name = "metodo", nullable = false, length = 20)
    private String metodo;

    // === Estado del pago ===
    @Column(name = "estado", length = 20, nullable = false)
    private String estado; // PENDIENTE, COMPLETADO, REEMBOLSADO, FALLIDO

    // === Referencia o código de operación (opcional) ===
    @Column(name = "referencia", length = 100)
    private String referencia;

    // === Usuario o sistema que registró el pago ===
    @Column(name = "registrado_por", length = 50)
    private String registradoPor;

    // === Auditoría temporal ===
    @Column(name = "pagado_en", nullable = false)
    private LocalDateTime pagadoEn;

    @PrePersist
    void alPagar() {
        if (this.pagadoEn == null) this.pagadoEn = LocalDateTime.now();
        if (this.estado == null || this.estado.isBlank()) this.estado = "COMPLETADO";
    }

    // --- Métodos auxiliares ---
    public void marcarReembolsado(String referencia) {
        this.estado = "REEMBOLSADO";
        this.referencia = referencia;
    }

    public void marcarFallido(String referencia) {
        this.estado = "FALLIDO";
        this.referencia = referencia;
    }

    public void marcarPendiente() {
        this.estado = "PENDIENTE";
    }

    public boolean esCompletado() {
        return "COMPLETADO".equalsIgnoreCase(this.estado);
    }
}
