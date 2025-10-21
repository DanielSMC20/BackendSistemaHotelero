package Hotel.jwt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cliente asociado
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Clientes cliente;

    // Habitación reservada
    @ManyToOne(optional = false)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @Column(name = "fecha_check_in", nullable = false)
    private LocalDate fechaCheckIn;

    @Column(name = "fecha_check_out", nullable = false)
    private LocalDate fechaCheckOut;

    @Column(name = "estado", length = 20)
    private String estado; // BOOKED, CHECKED_IN, CANCELLED, etc.

    @Column(name = "precio_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioTotal;

    @ManyToOne(optional = true)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne @JoinColumn(name = "checkin_by")
    private Usuario checkinBy;

    @ManyToOne @JoinColumn(name = "checkout_by")
    private Usuario checkoutBy;

    // Si quisieras persistir los pagos en el futuro:
    // @Column(name = "monto_pagado", precision = 12, scale = 2)
    // private BigDecimal montoPagado = BigDecimal.ZERO;
    //
    // @Column(name = "estado_pago", length = 20)
    // private String estadoPago = "PENDIENTE";
}
