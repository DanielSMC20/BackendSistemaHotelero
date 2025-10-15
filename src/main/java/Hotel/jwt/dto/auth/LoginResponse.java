package Hotel.jwt.dto.auth;

import Hotel.jwt.entity.Usuario;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Usuario usuario;
    public LoginResponse(String token, Usuario usuario){ this.token = token;
        this.usuario = usuario;
    }


}