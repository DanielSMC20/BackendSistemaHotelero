package Hotel.jwt.dto.auth;

import Hotel.jwt.entity.Rol;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String nombres;
    private String apellidos;
    private String pais;
    private Rol role;      // Como tu entidad usa enum Rol
    private String clave;  // Opcional, para cambiar contraseña
}