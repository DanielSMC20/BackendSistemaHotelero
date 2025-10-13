package Hotel.jwt.dto.customer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String documentId;
}