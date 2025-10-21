package Hotel.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "facturas",
        uniqueConstraints = @UniqueConstraint(name = "uk_factura_numero", columnNames = "numero"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Relación con reserva ===
    @OneToOne(optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    // === Tipo de comprobante ===
    @Column(name = "tipo", nullable = false, length = 15)
    private String tipo; // BOLETA / FACTURA

    // === Datos monetarios ===
    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "impuesto", precision = 12, scale = 2)
    private BigDecimal impuesto;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    // === Estado ===
    @Column(name = "estado", nullable = false, length = 20)
    private String estado; // PENDIENTE / PAGADA / ANULADA

    // === Identificación del comprobante ===
    @Column(name = "serie", length = 10)
    private String serie; // Ej: F001, B001

    @Column(name = "numero", unique = true, nullable = false, length = 30)
    private String numero;

    // === Auditoría ===
    @Column(name = "emitida_en", nullable = false)
    private LocalDateTime emitidaEn;

    @Column(name = "pagada_en")
    private LocalDateTime pagadaEn;

    @PrePersist
    void prePersist() {
        if (emitidaEn == null) emitidaEn = LocalDateTime.now();
        if (estado == null) estado = "PENDIENTE";
        if (tipo == null) tipo = "BOLETA";
        if (numero == null || numero.isBlank()) {
            String fecha = LocalDateTime.now().toLocalDate().toString().replace("-", "");
            String rand = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            numero = "%s-%s".formatted(fecha, rand);
        }
    }

    // === Métodos auxiliares ===
    public void marcarPagada() {
        this.estado = "PAGADA";
        this.pagadaEn = LocalDateTime.now();
    }

    public void marcarAnulada() {
        this.estado = "ANULADA";
    }
}
