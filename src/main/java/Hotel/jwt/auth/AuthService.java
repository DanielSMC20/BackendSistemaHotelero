package Hotel.jwt.auth;

import Hotel.jwt.dto.auth.AuthResponse;
import Hotel.jwt.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest req);
}