package Hotel.jwt.repository;


import Hotel.jwt.entity.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Clientes, Long> {

    List<Clientes> findByNombresCompletosContainingIgnoreCase(String nombresCompletos);
    Optional<Clientes> findByDocumento(String documento);
    boolean existsByDocumento(String documento);
    boolean existsByEmail(String email);

    @Transactional
    @Modifying
    @Query("""
        UPDATE Clientes c
        SET c.nombresCompletos   = :nombre,
            c.telefono = :telefono,
            c.email = :email
        WHERE c.documento = :documento
    """)
    void updateByDocumento(
            @Param("documento") String documento,
            @Param("nombre") String nombresCompletos,
            @Param("telefono") String telefono,
            @Param("email") String email
    );
}