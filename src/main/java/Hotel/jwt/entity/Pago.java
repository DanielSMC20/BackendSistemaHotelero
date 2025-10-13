package Hotel.jwt.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pago {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Column(name = "monto", nullable = false)
    private Double monto;

    @Column(name = "metodo", nullable = false, length = 20) // EFECTIVO, TARJETA, TRANSFERENCIA
    private String metodo;

    @Column(name = "pagado_en", nullable = false)
    private LocalDateTime pagadoEn;

    @PrePersist
    void alPagar() { this.pagadoEn = LocalDateTime.now(); }
}