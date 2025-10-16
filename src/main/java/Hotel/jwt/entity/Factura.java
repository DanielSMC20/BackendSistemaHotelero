package Hotel.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "facturas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Factura {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Una factura por reserva
    @OneToOne(optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "estado", nullable = false, length = 20) // PENDIENTE, PAGADA, ANULADA
    private String estado;

    @Column(name = "numero", unique = true, nullable = false, length = 30)
    private String numero;

    @Column(name = "emitida_en", nullable = false)
    private LocalDateTime emitidaEn;

    @PrePersist
    void pre() {
        if (emitidaEn == null) emitidaEn = LocalDateTime.now();
        if (estado == null) estado = "PENDIENTE";
        if (numero == null || numero.isBlank()) {
            String yyyymmdd = java.time.LocalDate.now().toString().replace("-", "");
            String rand = java.util.UUID.randomUUID().toString().substring(0,5).toUpperCase();
            numero = "INV-%s-%s".formatted(yyyymmdd, rand);
        }
    }
}
