package Hotel.jwt.repository;


import Hotel.jwt.entity.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Clientes, Long> {

    List<Clientes> findByNombresCompletosContainingIgnoreCase(String nombresCompletos);
    Optional<Clientes> findByDocumento(String documento);

}