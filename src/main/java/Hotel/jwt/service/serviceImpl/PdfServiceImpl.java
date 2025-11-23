package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.entity.Clientes;
import Hotel.jwt.entity.Habitacion;
import Hotel.jwt.entity.Pago;
import Hotel.jwt.entity.Reserva;
import Hotel.jwt.service.PdfService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class PdfServiceImpl implements PdfService {

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] generarComprobantePago(Pago pago) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Reserva reserva = pago.getReserva();
            Clientes cliente = reserva != null ? reserva.getCliente() : null;
            Habitacion hab = reserva != null ? reserva.getHabitacion() : null;

            // ===== Fuentes =====
            Font titleFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font hotelFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font boldFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font smallFont  = FontFactory.getFont(FontFactory.HELVETICA, 8);

            // =====================================================
            // 1) Determinar tipo de comprobante: BOLETA / FACTURA
            // =====================================================
            String tipoDocumento = cliente != null && cliente.getTipoDocumento() != null
                    ? cliente.getTipoDocumento().toUpperCase()
                    : "";

            // Regla sencilla: si es RUC20 → FACTURA, en otro caso → BOLETA
            boolean esFactura = "RUC20".equals(tipoDocumento);
            String tipoComprobante = esFactura ? "FACTURA" : "BOLETA DE VENTA";
            String serie = esFactura ? "F001" : "B001";
            String numeroComprobante = String.format("%s-%08d", serie, pago.getId());

            // =====================================================
            // 2) ENCABEZADO: Datos del hotel + tipo/serie/número
            // =====================================================
            PdfPTable header = new PdfPTable(new float[]{2f, 1.2f});
            header.setWidthPercentage(100);

            // Columna izquierda: info del hotel (ajusta a tus datos reales)
            PdfPCell hotelCell = new PdfPCell();
            hotelCell.setBorder(Rectangle.NO_BORDER);

            Paragraph hotelName = new Paragraph("HOSPEDAJE BETANIA", hotelFont);
            hotelName.setSpacingAfter(2f);

            hotelCell.addElement(hotelName);
            hotelCell.addElement(new Paragraph("RUC: 20481234567", normalFont));
            hotelCell.addElement(new Paragraph("Av. Los Geranios 123 - Ica, Perú", normalFont));
            hotelCell.addElement(new Paragraph("Tel: +51 999 999 999", normalFont));
            hotelCell.addElement(new Paragraph("Email: reservas@betania.com", normalFont));

            header.addCell(hotelCell);

            // Columna derecha: tipo comprobante + número
            PdfPCell compCell = new PdfPCell();
            compCell.setBorder(Rectangle.BOX);
            compCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph tipoCompP = new Paragraph(tipoComprobante, titleFont);
            tipoCompP.setAlignment(Element.ALIGN_CENTER);

            Paragraph serieNumP = new Paragraph(numeroComprobante, boldFont);
            serieNumP.setAlignment(Element.ALIGN_CENTER);

            compCell.addElement(tipoCompP);
            compCell.addElement(new Paragraph(" ", normalFont));
            compCell.addElement(serieNumP);

            header.addCell(compCell);
            document.add(header);

            document.add(new Paragraph(" ", normalFont)); // espacio

            // =====================================================
            // 3) DATOS DEL CLIENTE + RESERVA + PAGO
            // =====================================================

            // Nombre cliente (según NATURAL/JURIDICA)
            String nombreCliente;
            if (cliente != null) {
                if ("JURIDICA".equalsIgnoreCase(cliente.getTipoPersona())
                        && cliente.getRazonSocial() != null && !cliente.getRazonSocial().isBlank()) {
                    nombreCliente = cliente.getRazonSocial();
                } else if (cliente.getNombresCompletos() != null && !cliente.getNombresCompletos().isBlank()) {
                    nombreCliente = cliente.getNombresCompletos();
                } else {
                    nombreCliente = "CLIENTE";
                }
            } else {
                nombreCliente = "CLIENTE";
            }

            String docCliente = (cliente != null && cliente.getDocumento() != null)
                    ? cliente.getDocumento()
                    : "-";

            String tipoDocLabel = (cliente != null && cliente.getTipoDocumento() != null)
                    ? cliente.getTipoDocumento()
                    : "-";

            String email = (cliente != null && cliente.getEmail() != null)
                    ? cliente.getEmail()
                    : "-";

            String telefono = "-";
            if (cliente != null) {
                if (cliente.getTelefonoE164() != null && !cliente.getTelefonoE164().isBlank()) {
                    telefono = cliente.getTelefonoE164();
                } else if (cliente.getTelefonoCodigoPais() != null && cliente.getTelefono() != null) {
                    telefono = cliente.getTelefonoCodigoPais() + " " + cliente.getTelefono();
                }
            }

            String habitacionStr = (hab != null)
                    ? String.format("Hab. %s (%s)", hab.getNumero(), hab.getTipo())
                    : "-";

            String rangoHabitacion = (hab != null && hab.getRango() != null)
                    ? hab.getRango()
                    : "";

            String fechaEmision = pago.getPagadoEn() != null
                    ? pago.getPagadoEn().format(DATE_TIME_FMT)
                    : "-";

            String cajero = (pago.getRegistradoPor() != null && !pago.getRegistradoPor().isBlank())
                    ? pago.getRegistradoPor()
                    : "-";

            PdfPTable infoTable = new PdfPTable(new float[]{1.3f, 2.3f});
            infoTable.setWidthPercentage(100);

            infoTable.addCell(celdaEtiqueta("Cliente:", boldFont));
            infoTable.addCell(celdaValor(nombreCliente, normalFont));

            infoTable.addCell(celdaEtiqueta("Tipo / Nro Doc.:", boldFont));
            infoTable.addCell(celdaValor(tipoDocLabel + " - " + docCliente, normalFont));

            infoTable.addCell(celdaEtiqueta("Email:", boldFont));
            infoTable.addCell(celdaValor(email, normalFont));

            infoTable.addCell(celdaEtiqueta("Teléfono:", boldFont));
            infoTable.addCell(celdaValor(telefono, normalFont));

            infoTable.addCell(celdaEtiqueta("Habitación:", boldFont));
            infoTable.addCell(celdaValor(habitacionStr + (rangoHabitacion.isBlank() ? "" : " - " + rangoHabitacion), normalFont));

            infoTable.addCell(celdaEtiqueta("Fecha emisión:", boldFont));
            infoTable.addCell(celdaValor(fechaEmision, normalFont));

            infoTable.addCell(celdaEtiqueta("Método de pago:", boldFont));
            infoTable.addCell(celdaValor(pago.getMetodo(), normalFont));

            infoTable.addCell(celdaEtiqueta("Cajero:", boldFont));
            infoTable.addCell(celdaValor(cajero, normalFont));

            document.add(infoTable);

            document.add(new Paragraph(" ", normalFont)); // espacio

            // =====================================================
            // 4) DETALLE: por noches o por horas
            // =====================================================
            PdfPTable detalle = new PdfPTable(new float[]{4f, 1f, 1.2f, 1.2f});
            detalle.setWidthPercentage(100);
            detalle.setSpacingBefore(10f);

            // Encabezados
            detalle.addCell(celdaHeaderTabla("Descripción", boldFont));
            detalle.addCell(celdaHeaderTabla("Cant.", boldFont));
            detalle.addCell(celdaHeaderTabla("P. Unit", boldFont));
            detalle.addCell(celdaHeaderTabla("Importe", boldFont));

            // Total del pago
            BigDecimal total = BigDecimal.valueOf(pago.getMonto())
                    .setScale(2, RoundingMode.HALF_UP);

            // Calcular noches (si checkIn y checkOut son distintos)
            LocalDate checkIn = reserva != null ? reserva.getFechaCheckIn() : null;
            LocalDate checkOut = reserva != null ? reserva.getFechaCheckOut() : null;

            boolean tieneFechas = (checkIn != null && checkOut != null);
            long noches = 0;
            boolean modoNoches = false;
            if (tieneFechas) {
                noches = ChronoUnit.DAYS.between(checkIn, checkOut);
                if (noches > 0) {
                    modoNoches = true;
                }
            }

            int cantidad;
            String descServicio;

            if (modoNoches) {
                cantidad = (int) noches;
                String rangoFechas = String.format("del %s al %s",
                        checkIn.format(DATE_FMT),
                        checkOut.format(DATE_FMT));
                descServicio = String.format(
                        "Alojamiento %d noche(s) %s - Hab. %s",
                        cantidad,
                        rangoFechas,
                        hab != null ? hab.getNumero() : "N/A"
                );
            } else {
                // Asumimos reserva por horas (frontend controla horas y precioTotal)
                cantidad = 1;
                String fechaUnica = (checkIn != null)
                        ? checkIn.format(DATE_FMT)
                        : (checkOut != null ? checkOut.format(DATE_FMT) : "-");
                descServicio = String.format(
                        "Servicio de hospedaje por horas (%s) - Hab. %s",
                        fechaUnica,
                        hab != null ? hab.getNumero() : "N/A"
                );
            }

            BigDecimal cantidadBD = BigDecimal.valueOf(cantidad <= 0 ? 1 : cantidad);
            BigDecimal precioUnit = total.divide(cantidadBD, 2, RoundingMode.HALF_UP);

            // Fila detalle
            detalle.addCell(celdaValorTabla(descServicio, normalFont));
            detalle.addCell(celdaValorTabla(String.valueOf(cantidad), normalFont, Element.ALIGN_CENTER));
            detalle.addCell(celdaValorTabla("S/ " + precioUnit, normalFont, Element.ALIGN_RIGHT));
            detalle.addCell(celdaValorTabla("S/ " + total, normalFont, Element.ALIGN_RIGHT));

            document.add(detalle);

            // =====================================================
            // 5) TOTALES (con IGV 18%)
            // =====================================================
            BigDecimal divisor  = BigDecimal.valueOf(1.18);
            BigDecimal subtotal = total.divide(divisor, 2, RoundingMode.HALF_UP);
            BigDecimal igv      = total.subtract(subtotal).setScale(2, RoundingMode.HALF_UP);

            PdfPTable totales = new PdfPTable(new float[]{3f, 1.2f});
            totales.setWidthPercentage(50);
            totales.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totales.setSpacingBefore(10f);

            totales.addCell(celdaEtiqueta("Subtotal:", boldFont, Element.ALIGN_RIGHT));
            totales.addCell(celdaValor("S/ " + subtotal, normalFont, Element.ALIGN_RIGHT));

            totales.addCell(celdaEtiqueta("IGV 18%:", boldFont, Element.ALIGN_RIGHT));
            totales.addCell(celdaValor("S/ " + igv, normalFont, Element.ALIGN_RIGHT));

            totales.addCell(celdaEtiqueta("TOTAL:", boldFont, Element.ALIGN_RIGHT));
            totales.addCell(celdaValor("S/ " + total, boldFont, Element.ALIGN_RIGHT));

            document.add(totales);

            // =====================================================
            // 6) PIE DE PÁGINA
            // =====================================================
            document.add(new Paragraph(" ", normalFont));

            Paragraph gracias = new Paragraph("¡Gracias por su preferencia!", normalFont);
            gracias.setAlignment(Element.ALIGN_CENTER);
            document.add(gracias);

            Paragraph nota = new Paragraph(
                    "Este comprobante es emitido por el sistema del hotel y no reemplaza el comprobante electrónico autorizado por SUNAT.",
                    smallFont
            );
            nota.setAlignment(Element.ALIGN_CENTER);
            document.add(nota);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de comprobante", e);
        }
    }

    // ========= Helpers de layout =========

    private PdfPCell celdaEtiqueta(String texto, Font font) {
        return celdaEtiqueta(texto, font, Element.ALIGN_LEFT);
    }

    private PdfPCell celdaEtiqueta(String texto, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(align);
        return cell;
    }

    private PdfPCell celdaValor(String texto, Font font) {
        return celdaValor(texto, font, Element.ALIGN_LEFT);
    }

    private PdfPCell celdaValor(String texto, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(align);
        return cell;
    }

    private PdfPCell celdaHeaderTabla(String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new Color(230, 230, 230));
        return cell;
    }

    private PdfPCell celdaValorTabla(String texto, Font font) {
        return celdaValorTabla(texto, font, Element.ALIGN_LEFT);
    }

    private PdfPCell celdaValorTabla(String texto, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(align);
        return cell;
    }
}
