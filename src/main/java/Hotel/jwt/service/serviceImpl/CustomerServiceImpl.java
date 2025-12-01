package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.dto.customer.CustomerRequest;
import Hotel.jwt.entity.Clientes;
import Hotel.jwt.repository.CustomerRepository;
import Hotel.jwt.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;

    // === Helpers de validación ===
    private void validarTipoPersonaYCampos(Clientes c) {
        if (!"NATURAL".equalsIgnoreCase(c.getTipoPersona()) &&
                !"JURIDICA".equalsIgnoreCase(c.getTipoPersona())) {
            throw badRequest("tipoPersona inválido (NATURAL o JURIDICA).");
        }

        if (!StringUtils.hasText(c.getTipoDocumento())) {
            throw badRequest("tipoDocumento es obligatorio.");
        }
        if (!StringUtils.hasText(c.getDocumento())) {
            throw badRequest("documento es obligatorio.");
        }

        // Reglas básicas por tipo de persona
        if ("NATURAL".equalsIgnoreCase(c.getTipoPersona())) {
            if (!StringUtils.hasText(c.getNombresCompletos())) {
                throw badRequest("Para NATURAL, nombresCompletos es obligatorio.");
            }
            if (isRuc(c.getTipoDocumento())) {
                throw badRequest("Un NATURAL no debe registrar RUC (use DNI/CE/PASAPORTE).");
            }
        } else { // JURIDICA
            if (!StringUtils.hasText(c.getRazonSocial())) {
                throw badRequest("Para JURIDICA, razonSocial es obligatorio.");
            }
            if (!isRuc(c.getTipoDocumento())) {
                throw badRequest("Para JURIDICA, el tipoDocumento debe ser RUC10 o RUC20.");
            }
        }
    }

    private void validarDocumentoPorTipoYPais(Clientes c) {
        String td = c.getTipoDocumento().toUpperCase(Locale.ROOT);
        String doc = c.getDocumento().trim();

        switch (td) {
            case "DNI" -> {
                if (doc.length() != 8 || !doc.chars().allMatch(Character::isDigit)) {
                    throw badRequest("DNI inválido: debe tener 8 dígitos numéricos.");
                }
            }
            case "RUC10", "RUC20" -> {
                if (doc.length() != 11 || !doc.chars().allMatch(Character::isDigit)) {
                    throw badRequest("RUC inválido: debe tener 11 dígitos.");
                }
                if (td.equals("RUC10") && !doc.startsWith("10")) {
                    throw badRequest("RUC10 inválido: debe iniciar con '10'.");
                }
                if (td.equals("RUC20") && !doc.startsWith("20")) {
                    throw badRequest("RUC20 inválido: debe iniciar con '20'.");
                }
            }
            case "CE", "CARNET_EXTRANJERIA" -> {
                if (doc.length() < 8 || doc.length() > 12) {
                    throw badRequest("CE inválido: longitud esperada entre 8 y 12.");
                }
            }
            case "PASAPORTE" -> {
                if (doc.length() < 6 || doc.length() > 12) {
                    throw badRequest("Pasaporte inválido: longitud esperada entre 6 y 12.");
                }
            }
            default -> throw badRequest("tipoDocumento no soportado.");
        }

        // Validaciones por nacionalidad (si aplica)
        if (StringUtils.hasText(c.getNacionalidad()) &&
                !"PERU".equalsIgnoreCase(c.getNacionalidad())) {
            // Aquí podrías ajustar reglas por país si tienes tabla/parametrización
            // (por ahora solo validamos que exista tipoDoc coherente)
        }
    }

    private boolean isRuc(String tipoDocumento) {
        return "RUC10".equalsIgnoreCase(tipoDocumento) || "RUC20".equalsIgnoreCase(tipoDocumento);
    }

    private ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    // === Servicio público ===

    @Override
    @Transactional
    public Clientes create(Clientes c) {
        normalizar(c);
        validarTipoPersonaYCampos(c);
        validarDocumentoPorTipoYPais(c);

        if (repo.existsByTipoDocumentoAndDocumento(c.getTipoDocumento(), c.getDocumento())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un cliente con " + c.getTipoDocumento() + " " + c.getDocumento());
        }
        if (StringUtils.hasText(c.getEmail()) && repo.existsByEmailIgnoreCase(c.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un cliente con email " + c.getEmail());
        }

        try {
            // Estado por defecto
            if (!StringUtils.hasText(c.getEstado())) c.setEstado("ACTIVO");
            return repo.save(c);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Documento o email duplicado en la base de datos", ex);
        }
    }
    @Transactional
    public Clientes createFromRequest(CustomerRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("El request no puede ser nulo.");
        }
        if (!StringUtils.hasText(req.getDocumento())) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio.");
        }

        final String documento = req.getDocumento().trim();
        final String tipoDocReq = req.getTipoDocumento() == null
                ? ""
                : req.getTipoDocumento().trim().toUpperCase();

        // ==== Detectar si es persona jurídica (misma idea que en createWithCustomer) ====
        final boolean esJuridica = "RUC".equals(tipoDocReq) || documento.length() == 11;

        Clientes c = new Clientes();
        c.setDocumento(documento);

        // Tipo persona
        c.setTipoPersona(esJuridica ? "JURIDICA" : "NATURAL");

        // Tipo documento
        if (!tipoDocReq.isEmpty()) {
            c.setTipoDocumento(tipoDocReq);
        } else {
            c.setTipoDocumento(esJuridica ? "RUC" : "DNI");
        }

        // Nombre / Razón social
        if (StringUtils.hasText(req.getNombresCompletos())) {
            String valor = req.getNombresCompletos().trim();
            if (esJuridica) {
                c.setRazonSocial(valor);
            } else {
                c.setNombresCompletos(valor);
            }
        }

        // Email
        if (StringUtils.hasText(req.getEmail())) {
            c.setEmail(req.getEmail().trim());
        }

        // Teléfono (igual idea que en reserva)
        normalizeAndSetPhone(
                c,
                req.getPhoneCountryCode(),
                req.getTelefono(),
                req.getTelefonoE164()
        );

        // Estado por defecto (por si no lo llena normalizar)
        if (!StringUtils.hasText(c.getEstado())) {
            c.setEstado("ACTIVO");
        }

        // Delegar en tu create(Clientes c) que ya valida duplicados, etc.
        return create(c);
    }
    private void normalizeAndSetPhone(
            Clientes c,
            String phoneCountryCode,
            String telefono,
            String telefonoE164
    ) {
        String cc = phoneCountryCode == null ? "" : phoneCountryCode.trim();
        String tel = telefono == null ? "" : telefono.trim();
        String e164 = telefonoE164 == null ? "" : telefonoE164.trim();

        if (!StringUtils.hasText(cc)) {
            cc = "+51"; // default (ajusta si quieres)
        }

        if (StringUtils.hasText(e164)) {
            // Si ya viene armado, solo seteamos
            c.setTelefonoCodigoPais(cc);
            c.setTelefono(tel);
            c.setTelefonoE164(e164);
            return;
        }

        // Construimos E.164 simple: +51 + solo dígitos
        String soloDigitos = tel.replaceAll("\\D", "");
        String e164Build = cc + soloDigitos;

        c.setTelefonoCodigoPais(cc);
        c.setTelefono(tel);
        c.setTelefonoE164(e164Build);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Clientes> searchByName(String name) {
        if (!StringUtils.hasText(name)) {
            return repo.findAll();
        }
        return repo.findByNombresCompletosContainingIgnoreCase(name.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Clientes> findAll() {
        return repo.findAll();
    }

    // Nueva versión: busca por tipoDocumento + documento (más precisa)
    public Clientes findByTipoDocumentoAndDocumento(String tipoDocumento, String documento) {
        return repo.findByTipoDocumentoAndDocumento(tipoDocumento, documento)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente no encontrado: " + tipoDocumento + " " + documento
                ));
    }

    // Mantener compatibilidad si ya usas este método en otros lados:
    @Override
    @Transactional(readOnly = true)
    public Clientes findByDocumento(String documento) {
        // Si tienes muchos tipos de doc, mejor exigir tipoDocumento en el Controller.
        return repo.findAll().stream()
                .filter(c -> documento.equals(c.getDocumento()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente no encontrado con documento: " + documento));
    }

    @Override
    @Transactional
    public Clientes updateByDocumento(String documento, Clientes c) {
        // Recomendado: migrar a update por (tipoDocumento, documento).
        Clientes existing = findByDocumento(documento);

        normalizar(c);
        if (StringUtils.hasText(c.getEmail()) && !c.getEmail().equalsIgnoreCase(existing.getEmail())
                && repo.existsByEmailIgnoreCase(c.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un cliente con email " + c.getEmail());
        }

        // Update por clave compuesta (si viene tipoDocumento)
        if (StringUtils.hasText(c.getTipoDocumento())) {
            repo.updateByTipoDocAndDocumento(
                    c.getTipoDocumento(), documento,
                    c.getNombresCompletos(),
                    c.getRazonSocial(),
                    c.getTelefono(),
                    c.getTelefonoFijo(),
                    c.getEmail(),
                    c.getNacionalidad()
            );
            // Refrescar 'existing' (opcional)
            existing = repo.findByTipoDocumentoAndDocumento(c.getTipoDocumento(), documento)
                    .orElse(existing);
        } else {
            // Fallback a settear en memoria y save (si no recibes tipoDocumento en update)
            existing.setNombresCompletos(c.getNombresCompletos());
            existing.setRazonSocial(c.getRazonSocial());
            existing.setTelefono(c.getTelefono());
            existing.setTelefonoFijo(c.getTelefonoFijo());
            existing.setEmail(c.getEmail());
            existing.setNacionalidad(c.getNacionalidad());
            repo.save(existing);
        }
        return existing;
    }

    // === Nuevos métodos útiles ===

    @Transactional
    public void cambiarEstado(Long id, String estado) {
        if (!"ACTIVO".equalsIgnoreCase(estado) && !"INACTIVO".equalsIgnoreCase(estado)) {
            throw badRequest("Estado inválido (use ACTIVO o INACTIVO).");
        }
        int rows = repo.changeEstado(id, estado.toUpperCase(Locale.ROOT));
        if (rows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado (id=" + id + ")");
        }
    }

    @Transactional(readOnly = true)
    public List<Clientes> buscarFlexible(String q, String tipoPersona, String estado) {
        return repo.searchFlexible(
                q == null ? "" : q.trim(),
                StringUtils.hasText(tipoPersona) ? tipoPersona.toUpperCase(Locale.ROOT) : null,
                StringUtils.hasText(estado) ? estado.toUpperCase(Locale.ROOT) : null
        );
    }

    private void normalizar(Clientes c) {
        if (StringUtils.hasText(c.getEmail())) {
            c.setEmail(c.getEmail().trim().toLowerCase(Locale.ROOT));
        }
        if (StringUtils.hasText(c.getTipoDocumento())) {
            c.setTipoDocumento(c.getTipoDocumento().trim().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(c.getTipoPersona())) {
            c.setTipoPersona(c.getTipoPersona().trim().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(c.getEstado())) {
            c.setEstado(c.getEstado().trim().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(c.getDocumento())) {
            c.setDocumento(c.getDocumento().trim());
        }
        if (c.getNacionalidad() != null) {
            c.setNacionalidad(c.getNacionalidad().trim());
        }
    }
}
