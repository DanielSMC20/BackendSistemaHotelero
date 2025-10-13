package Hotel.jwt.dto.user;
import Hotel.jwt.entity.Rol;
import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String country;
    private Rol role;
}