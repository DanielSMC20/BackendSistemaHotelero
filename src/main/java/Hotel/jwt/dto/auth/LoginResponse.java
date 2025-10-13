package Hotel.jwt.dto.auth;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    public LoginResponse(String token, String autenticaciónExitosa){ this.token = token; }


}