package Hotel.jwt.mapper;

import Hotel.jwt.dto.room.RoomResponse;
import Hotel.jwt.entity.Habitacion;

public class RoomMapper {
    public static RoomResponse toResponse(Habitacion r){
        if (r == null) return null;
        return RoomResponse.builder()
                .id(r.getId())
                .number(r.getNumero())
                .type(r.getTipo())
                .available(r.getDisponible())
                .pricePerNight(r.getPrecioPorNoche())
                .build();
    }
}