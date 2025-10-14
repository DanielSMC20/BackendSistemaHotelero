package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.entity.Clientes;
import Hotel.jwt.repository.CustomerRepository;
import Hotel.jwt.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;

    @Override
    @Transactional
    public Clientes create(Clientes c) {
        if (repo.existsByDocumento(c.getDocumento())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un cliente con documento " + c.getDocumento());
        }
        if (c.getEmail() != null && repo.existsByEmail(c.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un cliente con email " + c.getEmail());
        }

        try {
            return repo.save(c);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Documento o email duplicado en la base de datos", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Clientes> searchByName(String name) {
        if (name == null || name.isBlank()) {
            return repo.findAll();
        }
        return repo.findByNombresCompletosContainingIgnoreCase(name.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Clientes> findAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Clientes findByDocumento(String documento) {
        return repo.findByDocumento(documento)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente no encontrado con documento: " + documento
                ));
    }
}