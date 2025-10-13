package Hotel.jwt.service;

import Hotel.jwt.dto.payment.PaymentRequest;
import Hotel.jwt.dto.payment.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse record(PaymentRequest req);
    List<PaymentResponse> byReservation(Long reservationId);
}
