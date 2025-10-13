package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.entity.Usuario;
import Hotel.jwt.repository.UserRepository;
import Hotel.jwt.service.UserService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService { // <-- solo UserService

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Override
    public Usuario create(Usuario u) {
        u.setClave(encoder.encode(u.getClave()));
        return repo.save(u);
    }

    @Override
    public Usuario findByUsername(String username) {
        return repo.findByUsuario(username).orElse(null);
    }
}