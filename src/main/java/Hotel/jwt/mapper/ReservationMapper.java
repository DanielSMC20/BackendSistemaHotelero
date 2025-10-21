package Hotel.jwt.mapper;
import Hotel.jwt.dto.reservation.ReservationResponse;
import Hotel.jwt.entity.Reserva;

public class ReservationMapper {
    public static ReservationResponse toResponse(Reserva r){
        if (r == null) return null;
        return ReservationResponse.builder()
                .id(r.getId())
                .customerId(r.getCliente().getId())
                .customerName(r.getCliente().getNombresCompletos())
                .roomId(r.getHabitacion().getId())
                .roomNumber(r.getHabitacion().getNumero())
                .checkIn(r.getFechaCheckIn())
                .checkOut(r.getFechaCheckOut())
                .status(r.getEstado())
                .build();
    }
}