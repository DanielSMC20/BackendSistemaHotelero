package Hotel.jwt.service;

import Hotel.jwt.dto.payment.PaymentRequest;
import Hotel.jwt.dto.payment.PaymentResponse;
import Hotel.jwt.entity.Pago;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {
    PaymentResponse record(PaymentRequest req);
    List<PaymentResponse> byReservation(Long reservaId);
    List<PaymentResponse.DayTotal> sumByDay(LocalDateTime inicio, LocalDateTime fin);
    Pago registrarPago(Pago pago);
    Pago obtenerPago(Long id);
    List<Pago> listarPagos();
    Pago marcarPagado(Long id);
    Pago marcarReembolsado(Long id, String referencia);
    Pago marcarFallido(Long id, String referencia);
}

    // Extras posibles:
    // PaymentResponse refund(Long paymentId, String reference);

