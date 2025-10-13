package Hotel.jwt.repository;

import Hotel.jwt.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByReserva_Id(Long reservaId);


    @Query(value = """
        SELECT DATE(p.paid_at) AS day, SUM(p.amount) AS total
        FROM payments p
        WHERE p.paid_at BETWEEN :start AND :end
        GROUP BY DATE(p.paid_at)
        ORDER BY day
        """, nativeQuery = true)
    List<Object[]> sumByDay(LocalDateTime start, LocalDateTime end);
}