package Hotel.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reservas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Clientes cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @Column(name = "fecha_check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "fecha_check_out", nullable = false)
    private LocalDate checkOut;

    @Column(name = "estado", length = 20) // BOOKED, CHECKED_IN, CANCELLED, etc.
    private String estado;

    @Column(name = "precio_total", nullable = false)
    private BigDecimal precioTotal; // Monto total de la reserva
}
