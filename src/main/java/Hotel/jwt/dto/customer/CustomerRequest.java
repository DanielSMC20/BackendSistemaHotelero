package Hotel.jwt.dto.customer;


import lombok.Data;

@Data
public class CustomerRequest {
    private String documento;
    private String tipoDocumento;
    private String nombresCompletos;
    private String email;

    private String phoneCountryCode;
    private String telefono;
    private String telefonoE164;
}