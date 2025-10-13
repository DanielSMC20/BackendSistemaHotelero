package Hotel.jwt.mapper;

import Hotel.jwt.dto.customer.CustomerResponse;
import Hotel.jwt.entity.Clientes;

public class CustomerMapper {
    public static CustomerResponse toResponse(Clientes c){
        if (c == null) return null;
        return CustomerResponse.builder()
                .id(c.getId())
                .fullName(c.getNombresCompletos())
                .email(c.getEmail())
                .phone(c.getTelefono())
                .documentId(c.getDocumento())
                .build();
    }
}
