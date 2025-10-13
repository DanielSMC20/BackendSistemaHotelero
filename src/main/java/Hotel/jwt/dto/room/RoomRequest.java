package Hotel.jwt.dto.room;
import Hotel.jwt.entity.TipoHabitacion;
import lombok.Data;

@Data
public class RoomRequest {
    private String number;
    private TipoHabitacion type;
    private Boolean available;
    private Double pricePerNight;
}