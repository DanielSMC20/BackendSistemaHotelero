package Hotel.jwt.repository;

import Hotel.jwt.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Factura, Long> {

    Optional<Factura> findByReserva_Id(Long reservaId);

    // OJO: el nombre del método debe respetar el campo exacto -> Numero
    Optional<Factura> findByNumero(String numero);

    List<Factura> findByEstado(String estado);

    List<Factura> findByTipo(String tipo);

    List<Factura> findByEmitidaEnBetween(LocalDateTime desde, LocalDateTime hasta);

    Optional<Factura> findTopBySerieOrderByIdDesc(String serie);

    boolean existsBySerieAndNumero(String serie, String numero);


}
