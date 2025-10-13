package Hotel.jwt.repository;
import Hotel.jwt.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface RoomRepository extends JpaRepository<Habitacion, Long> {
    List<Habitacion> findByDisponibleTrue();   // <-- disponible, no available
    List<Habitacion> findByDisponibleFalse();
    Optional<Habitacion> findByNumero(String numero);
    Optional<Habitacion> findByNumeroIgnoreCase(String numero);

    long countByDisponibleTrue();
    long countByDisponibleFalse();
    boolean existsByNumero(String numero);
}