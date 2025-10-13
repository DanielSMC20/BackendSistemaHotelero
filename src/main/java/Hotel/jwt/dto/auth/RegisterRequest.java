package Hotel.jwt.dto.auth;

import Hotel.jwt.entity.Rol;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @JsonProperty("usuario")
    @JsonAlias({"username"})
    @NotBlank(message = "El usuario es obligatorio")
    @Size(max = 80, message = "El usuario no debe exceder 80 caracteres")
    private String usuario;

    @JsonProperty("clave")
    @JsonAlias({"password"})
    @NotBlank(message = "La clave es obligatoria")
    private String clave;

    @JsonProperty("nombres")
    @JsonAlias({"firstName"})
    private String nombres;

    @JsonProperty("apellidos")
    @JsonAlias({"lastName"})
    private String apellidos;

    @JsonProperty("pais")
    @JsonAlias({"country"})
    private String pais;

    @JsonProperty("rol")
    @JsonAlias({"role"})
    private Rol rol;  // ADMIN, RECEPCIONISTA, GERENTE
}