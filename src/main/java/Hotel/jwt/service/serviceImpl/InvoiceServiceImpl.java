package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.dto.invoice.CreateInvoiceRequest;
import Hotel.jwt.dto.invoice.InvoiceResponse;
import Hotel.jwt.entity.Factura;
import Hotel.jwt.entity.Reserva;
import Hotel.jwt.exception.BusinessException;
import Hotel.jwt.exception.NotFoundException;
import Hotel.jwt.repository.InvoiceRepository;
import Hotel.jwt.repository.ReservationRepository;
import Hotel.jwt.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private static final BigDecimal DEFAULT_TAX = new BigDecimal("0.18");
    private final InvoiceRepository invoiceRepo;
    private final ReservationRepository reservationRepo;

    // ========= Public API =========

    @Override
    @Transactional
    public InvoiceResponse issue(CreateInvoiceRequest req) {
        // 1) Validar reserva
        Reserva res = reservationRepo.findById(req.getReservationId())
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + req.getReservationId()));

        // 2) Idempotencia: una reserva -> una factura
        invoiceRepo.findByReserva_Id(res.getId()).ifPresent(i -> {
            throw new BusinessException("La reserva ya fue facturada con número: " + i.getNumero());
        });

        // 3) Tipo y serie
        String tipo = normalizeType(req.getType()); // "BOLETA"/"FACTURA"
        String serie = decideSerie(tipo, req.getSerie()); // "B001"/"F001" o la que mandes

        // 4) Monto base (override o precio de la reserva)
        BigDecimal baseTotal = (req.getOverrideTotal() != null)
                ? req.getOverrideTotal()
                : safe(res.getPrecioTotal());
        if (baseTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El total de la reserva no puede ser negativo.");
        }

        // 5) Impuesto (IGV)
        BigDecimal taxRate = (req.getTaxRate() != null) ? req.getTaxRate() : DEFAULT_TAX;

        // Aquí asumimos que el precio de reserva es PRECIO FINAL (incluye IGV).
        // Si prefieres que sea precio SIN IGV, cambia la rama de cálculo.
        BigDecimal total = scale2(baseTotal);
        BigDecimal subtotal = scale2(total.divide(BigDecimal.ONE.add(taxRate), 2, RoundingMode.HALF_UP));
        BigDecimal impuesto = scale2(total.subtract(subtotal));

        // 6) Número correlativo: SERIE + "-" + yyyymmdd + "-" + 5chars
        String numero = generateInvoiceNumber(serie);

        // 7) Crear entidad
        Factura inv = Factura.builder()
                .reserva(res)
                .tipo(tipo)
                .serie(serie)
                .numero(numero)
                .subtotal(subtotal)
                .impuesto(impuesto)
                .total(total)
                .estado("PENDIENTE")
                .build();

        inv = invoiceRepo.save(inv);

        // 8) Marcar como pagada si lo pides
        if (req.isMarkPaid()) {
            inv.marcarPagada();
            invoiceRepo.save(inv);
        }

        // 9) Respuesta
        return toResponse(inv);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getByReservation(Long reservaId) {
        var inv = invoiceRepo.findByReserva_Id(reservaId)
                .orElseThrow(() -> new NotFoundException("Factura no encontrada para la reserva " + reservaId));
        return toResponse(inv);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse get(Long id) {
        var inv = invoiceRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Factura no encontrada: " + id));
        return toResponse(inv);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getByNumber(String numero) {
        var inv = invoiceRepo.findByNumero(numero)
                .orElseThrow(() -> new NotFoundException("Factura no encontrada: " + numero));
        return toResponse(inv);
    }

    @Transactional
    public InvoiceResponse markPaid(Long id) {
        var inv = invoiceRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Factura no encontrada: " + id));

        if ("ANULADA".equalsIgnoreCase(inv.getEstado())) {
            throw new BusinessException("No se puede pagar una factura ANULADA.");
        }
        if ("PAGADA".equalsIgnoreCase(inv.getEstado())) {
            return toResponse(inv); // idempotente
        }

        inv.marcarPagada();
        invoiceRepo.save(inv);
        return toResponse(inv);
    }

    @Transactional
    public InvoiceResponse cancel(Long id) {
        var inv = invoiceRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Factura no encontrada: " + id));

        if ("PAGADA".equalsIgnoreCase(inv.getEstado())) {
            throw new BusinessException("No se puede anular una factura PAGADA.");
        }
        if ("ANULADA".equalsIgnoreCase(inv.getEstado())) {
            return toResponse(inv); // idempotente
        }

        inv.marcarAnulada();
        invoiceRepo.save(inv);
        return toResponse(inv);
    }

    @Transactional(readOnly = true)
    public java.util.List<InvoiceResponse> listByDate(LocalDateTime desde, LocalDateTime hasta, String tipo, String estado) {
        java.util.List<Factura> base = invoiceRepo.findByEmitidaEnBetween(desde, hasta);
        return base.stream()
                .filter(f -> tipo == null || tipo.equalsIgnoreCase(f.getTipo()))
                .filter(f -> estado == null || estado.equalsIgnoreCase(f.getEstado()))
                .map(this::toResponse)
                .toList();
    }

    // ========= Helpers =========

    private String normalizeType(String input) {
        String t = (input == null || input.isBlank()) ? "BOLETA" : input.trim().toUpperCase();
        if (!t.equals("BOLETA") && !t.equals("FACTURA")) {
            throw new BusinessException("Tipo de comprobante inválido (use BOLETA o FACTURA).");
        }
        return t;
    }

    private String decideSerie(String tipo, String serie) {
        if (serie != null && !serie.isBlank()) return serie.trim().toUpperCase();
        return "FACTURA".equalsIgnoreCase(tipo) ? "F001" : "B001";
    }

    private BigDecimal safe(BigDecimal n) {
        return (n == null) ? BigDecimal.ZERO : n;
    }

    private BigDecimal scale2(BigDecimal n) {
        return n.setScale(2, RoundingMode.HALF_UP);
    }

    private String generateInvoiceNumber(String serie) {
        String yyyymmdd = LocalDate.now().toString().replace("-", "");
        String rand5 = java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return serie + "-" + yyyymmdd + "-" + rand5;
    }

    private InvoiceResponse toResponse(Factura inv) {
        return InvoiceResponse.builder()
                .id(inv.getId())
                .reservationId(inv.getReserva().getId())
                .type(inv.getTipo())
                .serie(inv.getSerie())
                .number(inv.getNumero())
                .subtotal(inv.getSubtotal())
                .tax(inv.getImpuesto())
                .total(inv.getTotal())
                .status(inv.getEstado())
                .issuedAt(inv.getEmitidaEn())
                .paidAt(inv.getPagadaEn())
                .build();
    }
}
