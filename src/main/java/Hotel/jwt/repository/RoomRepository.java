package Hotel.jwt.repository;

import Hotel.jwt.entity.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Habitacion, Long> {

    Optional<Habitacion> findByNumero(String numero);
    Optional<Habitacion> findByNumeroIgnoreCase(String numero);
    boolean existsByNumero(String numero);

    // Disponibles por estado
    @Query("SELECT h FROM Habitacion h WHERE UPPER(h.estado) = 'DISPONIBLE'")
    List<Habitacion> findAvailable();

    @Query("SELECT h FROM Habitacion h WHERE UPPER(h.estado) = 'OCUPADA'")
    List<Habitacion> findOccupied();

    // Contadores por estado (útil para dashboard/reportes)
    long countByEstadoIgnoreCase(String estado);

    // Para autogenerar HB-###
    @Query("SELECT MAX(h.codigo) FROM Habitacion h WHERE h.codigo LIKE 'HB-%'")
    String findMaxCodigoHabitacion();
}
