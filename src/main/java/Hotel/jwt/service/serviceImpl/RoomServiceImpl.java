// Hotel/jwt/service/serviceImpl/RoomServiceImpl.java
package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.entity.Habitacion;
import Hotel.jwt.repository.RoomRepository;
import Hotel.jwt.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository repo;

    @Override
    @Transactional
    public Habitacion create(Habitacion h) {
        normalizar(h);
        if (!StringUtils.hasText(h.getNumero())) {
            throw new IllegalArgumentException("El número de habitación es obligatorio.");
        }
        if (repo.existsByNumero(h.getNumero())) {
            throw new IllegalArgumentException("Ya existe una habitación con número " + h.getNumero());
        }
        if (h.getEstado() == null) h.setEstado("DISPONIBLE");
        trySetCodigoSecuencial(h);
        return repo.save(h);
    }

    @Override
    @Transactional
    public Habitacion update(Long id, Habitacion changes) {
        Habitacion h = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));

        if (StringUtils.hasText(changes.getNumero())
                && !changes.getNumero().equalsIgnoreCase(h.getNumero())) {
            if (repo.existsByNumero(changes.getNumero())) {
                throw new IllegalArgumentException("Ya existe una habitación con número " + changes.getNumero());
            }
            h.setNumero(changes.getNumero().trim().toUpperCase());
        }

        if (changes.getTipo() != null) h.setTipo(changes.getTipo());
        if (StringUtils.hasText(changes.getEstado())) h.setEstado(changes.getEstado().trim().toUpperCase());
        if (changes.getCapacidad() != null) h.setCapacidad(changes.getCapacidad());
        if (changes.getCamas() != null) h.setCamas(changes.getCamas());
        if (StringUtils.hasText(changes.getRango())) h.setRango(changes.getRango().trim());
        if (changes.getPrecioPorNoche() != null) h.setPrecioPorNoche(changes.getPrecioPorNoche());
        if (changes.getPrecioPorHora() != null) h.setPrecioPorHora(changes.getPrecioPorHora());
        if (StringUtils.hasText(changes.getDetalles())) h.setDetalles(changes.getDetalles().trim());

        return repo.save(h);
    }
    @Override
    @Transactional(readOnly = true)
    public Habitacion getByNumero(String numero) {
        return repo.findByNumeroIgnoreCase(numero)
                .orElseThrow(() ->
                        new IllegalArgumentException("Habitación no encontrada con número: " + numero));
    }
    @Override @Transactional(readOnly = true)
    public List<Habitacion> listAvailable() { return repo.findAvailable(); }

    @Override @Transactional(readOnly = true)
    public List<Habitacion> findAll() { return repo.findAll(); }

    @Override @Transactional
    public void marcarOcupada(Long id) { changeEstado(id, "OCUPADA"); }

    @Override @Transactional
    public void marcarDisponible(Long id) { changeEstado(id, "DISPONIBLE"); }

    @Override @Transactional
    public void marcarMantenimiento(Long id) { changeEstado(id, "MANTENIMIENTO"); }

    // ── helpers ─────────────────────────────────────────────
    private void changeEstado(Long id, String estado) {
        Habitacion h = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));
        h.setEstado(estado);
        repo.save(h);
    }

    private void normalizar(Habitacion h) {
        if (StringUtils.hasText(h.getNumero())) h.setNumero(h.getNumero().trim().toUpperCase());
        if (StringUtils.hasText(h.getRango())) h.setRango(h.getRango().trim());
    }

    private void trySetCodigoSecuencial(Habitacion h) {
        if (h.getCodigo() == null || h.getCodigo().isBlank()) {
            String ultimo = repo.findMaxCodigoHabitacion(); // ej "HB-007"
            String next = "HB-001";
            if (ultimo != null && ultimo.startsWith("HB-")) {
                try {
                    int n = Integer.parseInt(ultimo.substring(3));
                    next = String.format("HB-%03d", n + 1);
                } catch (Exception ignored) {}
            }
            h.setCodigo(next);
        }
    }
}
