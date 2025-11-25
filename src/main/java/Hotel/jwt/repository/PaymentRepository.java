package Hotel.jwt.repository;

import Hotel.jwt.entity.Pago;
import Hotel.jwt.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findTopByReservaOrderByPagadoEnDesc(Reserva reserva);

    List<Pago> findByReserva_Id(Long reservaId);

    List<Pago> findByEstado(String estado);

    List<Pago> findByMetodo(String metodo);

    boolean existsByReferencia(String referencia);


    @Query(value = """
        SELECT 
            DATE(p.pagado_en) AS dia,
            SUM(p.monto)      AS total
        FROM pagos p
        WHERE p.pagado_en BETWEEN :inicio AND :fin
        GROUP BY DATE(p.pagado_en)
        ORDER BY dia
        """, nativeQuery = true)
    List<Object[]> sumByDay(@Param("inicio") LocalDateTime inicio,
                            @Param("fin") LocalDateTime fin);

    @Query("""
       SELECT COALESCE(SUM(p.monto), 0)
       FROM Pago p
       WHERE p.reserva.id = :reservaId
         AND UPPER(p.estado) = 'COMPLETADO'
       """)
    Double sumCompletadosByReserva(Long reservaId);
}
