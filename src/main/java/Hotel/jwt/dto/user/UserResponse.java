package Hotel.jwt.dto.user;

import Hotel.jwt.entity.Rol;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Integer id;
    private String username;
    private String firstName;
    private String lastName;
    private String country;
    private Rol role;
}