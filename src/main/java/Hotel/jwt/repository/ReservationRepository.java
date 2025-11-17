package Hotel.jwt.repository;

import Hotel.jwt.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByEstado(String estado);

    // ✅ Reportes entre fechas
    @Query("""
        SELECT r
        FROM Reserva r
        WHERE (r.fechaCheckIn BETWEEN :inicio AND :fin)
           OR (r.fechaCheckOut BETWEEN :inicio AND :fin)
        ORDER BY r.fechaCheckIn ASC
    """)
    List<Reserva> findByRangoFechas(@Param("inicio") LocalDate inicio,
                                    @Param("fin") LocalDate fin);

    // ✅ Dashboard o reservas activas
    @Query("""
        SELECT r
        FROM Reserva r
        WHERE r.estado IN ('RESERVADO','CHECKED_IN')
        ORDER BY r.fechaCheckIn DESC
    """)
    List<Reserva> findReservasActivas();

    // ✅ Ocupación: alojados en una fecha concreta
    @Query("""
      SELECT COUNT(r)
      FROM Reserva r
      WHERE r.fechaCheckIn <= :d
        AND r.fechaCheckOut  > :d
        AND (r.estado IS NULL OR r.estado <> 'CANCELLED')
    """)
    long countActiveByDate(@Param("d") LocalDate d);

    // ✅ Este método derivado es 100% válido para tu entidad actual
    List<Reserva> findByFechaCheckOutBetween(LocalDate start, LocalDate end);
    @Query("""
        SELECT COUNT(r) > 0
        FROM Reserva r
        WHERE r.habitacion.id = :roomId
          AND r.estado IN :estados
          AND r.fechaCheckIn < :endDate
          AND r.fechaCheckOut > :startDate
    """)
    boolean existsOverlappingReservation(
            Long roomId,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            java.util.List<String> estados
    );
}
