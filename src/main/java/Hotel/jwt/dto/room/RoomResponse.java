package Hotel.jwt.dto.room;


import Hotel.jwt.entity.TipoHabitacion;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomResponse {
    private Long id;
    private String number;
    private TipoHabitacion type;
    private Boolean available;
    private Double pricePerNight;
}