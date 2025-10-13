package Hotel.jwt.service.serviceImpl;
import Hotel.jwt.dto.invoice.CreateInvoiceRequest;
import Hotel.jwt.dto.invoice.InvoiceResponse;
import Hotel.jwt.entity.Factura;
import Hotel.jwt.entity.Reserva;
import Hotel.jwt.exception.BusinessException;
import Hotel.jwt.exception.NotFoundException;
import Hotel.jwt.repository.InvoiceRepository;
import Hotel.jwt.repository.PaymentRepository;
import Hotel.jwt.repository.ReservationRepository;
import Hotel.jwt.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final ReservationRepository reservationRepo;
    private final PaymentRepository paymentRepo;

    @Override
    public InvoiceResponse issue(CreateInvoiceRequest req) {
        Reserva res = reservationRepo.findById(req.getReservationId())
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + req.getReservationId()));

        invoiceRepo.findByReserva_Id(res.getId()).ifPresent(i -> {
            throw new BusinessException("La reserva ya fue facturada con número: " + i.getNumero());
        });

        // Total = suma de pagos registrados a la reserva
        double total = paymentRepo.findByReserva_Id(res.getId()).stream()
                .mapToDouble(p -> p.getMonto() != null ? p.getMonto() : 0.0)
                .sum();

        // Generar número de factura
        String prefix = (req.getNumberPrefix() == null || req.getNumberPrefix().isBlank()) ? "INV" : req.getNumberPrefix().trim();
        String number = generateInvoiceNumber(prefix);

        Factura inv = Factura.builder()
                .reserva(res)
                .numero(number)
                .total(total)
                .build();

        inv = invoiceRepo.save(inv);

        return InvoiceResponse.builder()
                .id(inv.getId())
                .number(inv.getNumero())
                .reservationId(res.getId())
                .total(inv.getTotal())
                .issuedAt(inv.getEmitidaEn())
                .build();
    }

    @Override
    public InvoiceResponse getByReservation(Long reservaId) {
        var inv = invoiceRepo.findByReserva_Id(reservaId)
                .orElseThrow(() -> new NotFoundException("Factura no encontrada para la reserva " + reservaId));
        return InvoiceResponse.builder()
                .id(inv.getId())
                .number(inv.getNumero())
                .reservationId(inv.getReserva().getId())
                .total(inv.getTotal())
                .issuedAt(inv.getEmitidaEn())
                .build();
    }

    @Override
    public InvoiceResponse get(Long id) {
        var inv = invoiceRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Factura no encontrada: " + id));
        return InvoiceResponse.builder()
                .id(inv.getId())
                .number(inv.getNumero())
                .reservationId(inv.getReserva().getId())
                .total(inv.getTotal())
                .issuedAt(inv.getEmitidaEn())
                .build();
    }

    private String generateInvoiceNumber(String prefix) {
        // INV-YYYYMMDD-XXXXX (random corto)
        String yyyymmdd = LocalDate.now().toString().replace("-", "");
        String rand = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return "%s-%s-%s".formatted(prefix, yyyymmdd, rand);
    }
}