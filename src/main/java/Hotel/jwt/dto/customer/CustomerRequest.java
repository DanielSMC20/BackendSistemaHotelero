package Hotel.jwt.dto.customer;


import lombok.Data;

@Data
public class CustomerRequest {
    private String fullName;
    private String email;
    private String phone;
    private String documentId;
}