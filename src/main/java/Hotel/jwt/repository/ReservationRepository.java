package Hotel.jwt.repository;

import Hotel.jwt.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
public interface ReservationRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByCliente_Id(Long clienteId);

    // Opción A (derivada): "activa en una fecha D" ≡ checkIn <= D && checkOut >= D
    List<Reserva> findByCheckInLessThanEqualAndCheckOutGreaterThanEqual(LocalDate d1, LocalDate d2);

    // Opción B (JPQL legible): misma lógica que arriba
    @Query("""
           SELECT r
           FROM Reserva r
           WHERE r.checkIn <= :date AND r.checkOut >= :date
           """)
    List<Reserva> findActiveOnDate(@Param("date") LocalDate date);

    List<Reserva> findByCheckOutGreaterThanEqualAndCheckInLessThanEqual(LocalDate start, LocalDate end);
    boolean existsByHabitacion_IdAndCheckOutGreaterThanEqualAndCheckInLessThanEqual(
            Long habitacionId, LocalDate checkIn, LocalDate checkOut);
}