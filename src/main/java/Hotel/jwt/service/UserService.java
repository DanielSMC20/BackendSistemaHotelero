package Hotel.jwt.service;

import Hotel.jwt.entity.Usuario;

public interface UserService {
    Usuario create(Usuario u);
    Usuario findByUsername(String usuario);
}