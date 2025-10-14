package Hotel.jwt.service.serviceImpl;

import Hotel.jwt.entity.Clientes;
import Hotel.jwt.repository.CustomerRepository;
import Hotel.jwt.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;

    @Override
    public Clientes create(Clientes c) {
        return repo.save(c);
    }

    @Override
    public List<Clientes> searchByName(String name) {
        if (name == null || name.isBlank()) {
            return repo.findAll();
        }
        return repo.findByNombresCompletosContainingIgnoreCase(name);
    }
    @Override
    public List<Clientes> findAll() {
        return repo.findAll();
    }
    @Override
    public Clientes findByDocumento(String documento) {
        return repo.findByDocumento(documento)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con documento: " + documento));
    }
}