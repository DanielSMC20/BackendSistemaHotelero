package Hotel.jwt.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "facturas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Factura {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero; // p.ej. INV-20251011-00001

    @Column(name = "total", nullable = false)
    private Double total;

    @Column(name = "emitida_en", nullable = false)
    private LocalDateTime emitidaEn;

    @PrePersist
    void alEmitir() { this.emitidaEn = LocalDateTime.now(); }
}