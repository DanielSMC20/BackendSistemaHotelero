package Hotel.jwt.mapper;
import Hotel.jwt.dto.user.UserResponse;
import Hotel.jwt.entity.Usuario;

public class UserMapper {
    public static UserResponse toResponse(Usuario u){
        if (u == null) return null;
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsuario())
                .firstName(u.getNombres())
                .lastName(u.getApellidos())
                .country(u.getPais())
                .role(u.getRole())
                .build();
    }
}