package Hotel.jwt.service;


import Hotel.jwt.dto.invoice.CreateInvoiceRequest;
import Hotel.jwt.dto.invoice.InvoiceResponse;

public interface InvoiceService {
    InvoiceResponse issue(CreateInvoiceRequest req);     // emitir factura
    InvoiceResponse getByReservation(Long reservationId);
    InvoiceResponse get(Long id);
}