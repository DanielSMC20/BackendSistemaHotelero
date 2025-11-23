package Hotel.jwt.service;
import Hotel.jwt.entity.Pago;

public interface PdfService {
    byte[] generarComprobantePago(Pago pago);
}
