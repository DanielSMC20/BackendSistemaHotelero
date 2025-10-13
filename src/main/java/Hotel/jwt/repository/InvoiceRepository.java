package Hotel.jwt.repository;
import Hotel.jwt.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findByReserva_Id(Long reservaId);
    Optional<Factura> findBynumero(String numero);
}