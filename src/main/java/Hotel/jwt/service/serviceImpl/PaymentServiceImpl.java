package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.dto.payment.PaymentRequest;
import Hotel.jwt.dto.payment.PaymentResponse;
import Hotel.jwt.entity.Pago;
import Hotel.jwt.entity.Reserva;
import Hotel.jwt.exception.NotFoundException;
import Hotel.jwt.repository.PaymentRepository;
import Hotel.jwt.repository.ReservationRepository;
import Hotel.jwt.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final ReservationRepository reservationRepo;

    @Override
    public PaymentResponse record(PaymentRequest req) {
        Reserva res = reservationRepo.findById(req.getReservationId())
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + req.getReservationId()));

        Pago p = Pago.builder()
                .reserva(res)
                .monto(req.getAmount())
                .metodo(req.getMethod())
                .build();

        p = paymentRepo.save(p);

        return PaymentResponse.builder()
                .id(p.getId())
                .reservationId(res.getId())
                .amount(p.getMonto())
                .method(p.getMetodo())
                .paidAt(p.getPagadoEn())
                .build();
    }

    @Override
    public List<PaymentResponse> byReservation(Long reservaId) {
        return paymentRepo.findByReserva_Id(reservaId).stream()
                .map(p -> PaymentResponse.builder()
                        .id(p.getId())
                        .reservationId(p.getReserva().getId())
                        .amount(p.getMonto())
                        .method(p.getMetodo())
                        .paidAt(p.getPagadoEn())
                        .build())
                .toList();
    }
}