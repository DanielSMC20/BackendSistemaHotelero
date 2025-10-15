package Hotel.jwt.service.serviceImpl;
import Hotel.jwt.entity.Clientes;
import Hotel.jwt.entity.Habitacion;
import Hotel.jwt.repository.RoomRepository;
import Hotel.jwt.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository repo;

    @Override public Habitacion create(Habitacion r) { return repo.save(r); }
    @Override public List<Habitacion> listAvailable() { return repo.findByDisponibleTrue(); }

    @Override
    @Transactional(readOnly = true)
    public List<Habitacion> findAll() {
        return repo.findAll();
    }
}