package Hotel.jwt.repository;

import Hotel.jwt.entity.Clientes;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Clientes, Long> {

    // === Búsquedas básicas ===
    List<Clientes> findByNombresCompletosContainingIgnoreCase(String nombresCompletos);
    List<Clientes> findByEstado(String estado);
    List<Clientes> findByTipoPersonaAndEstado(String tipoPersona, String estado);

    // === Unicidad por documento (compuesto) y email ===
    Optional<Clientes> findByTipoDocumentoAndDocumento(String tipoDocumento, String documento);
    boolean existsByTipoDocumentoAndDocumento(String tipoDocumento, String documento);
    boolean existsByEmailIgnoreCase(String email);

    // === Búsqueda flexible por query (nombre/razón social/documento/email) ===
    @Query("""
        SELECT c FROM Clientes c
        WHERE (:q IS NULL OR :q = '' OR
               LOWER(c.nombresCompletos) LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(c.razonSocial)      LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(c.email)            LIKE LOWER(CONCAT('%', :q, '%')) OR
               c.documento               LIKE CONCAT('%', :q, '%'))
          AND (:tipoPersona IS NULL OR c.tipoPersona = :tipoPersona)
          AND (:estado IS NULL OR c.estado = :estado)
    """)
    List<Clientes> searchFlexible(@Param("q") String q,
                                  @Param("tipoPersona") String tipoPersona,
                                  @Param("estado") String estado);

    // === Update por (tipoDocumento, documento) ===
    @Transactional
    @Modifying
    @Query("""
        UPDATE Clientes c
           SET c.nombresCompletos = :nombresCompletos,
               c.razonSocial      = :razonSocial,
               c.telefono         = :telefono,
               c.telefonoFijo     = :telefonoFijo,
               c.email            = :email,
               c.nacionalidad     = :nacionalidad
         WHERE c.tipoDocumento = :tipoDocumento
           AND c.documento     = :documento
    """)
    int updateByTipoDocAndDocumento(@Param("tipoDocumento") String tipoDocumento,
                                    @Param("documento") String documento,
                                    @Param("nombresCompletos") String nombresCompletos,
                                    @Param("razonSocial") String razonSocial,
                                    @Param("telefono") String telefono,
                                    @Param("telefonoFijo") String telefonoFijo,
                                    @Param("email") String email,
                                    @Param("nacionalidad") String nacionalidad);

    // === Cambiar estado (ACTIVO/INACTIVO) ===
    @Transactional
    @Modifying
    @Query("""
        UPDATE Clientes c
           SET c.estado = :estado
         WHERE c.id = :id
    """)
    int changeEstado(@Param("id") Long id, @Param("estado") String estado);

    Optional<Clientes> findByDocumento(String documento);
    boolean existsByDocumento(String documento);

}
