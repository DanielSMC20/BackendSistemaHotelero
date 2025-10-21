package Hotel.jwt.service;

import Hotel.jwt.entity.Habitacion;

import java.util.List;

public interface RoomService {
    Habitacion create(Habitacion r);

    Habitacion update(Long id, Habitacion changes);   // <-- agrega esto

    List<Habitacion> listAvailable();

    List<Habitacion> findAll();

    // (opcional pero útil si ya los usas en controller)
    void marcarOcupada(Long id);

    void marcarDisponible(Long id);

    void marcarMantenimiento(Long id);

    Habitacion getByNumero(String numero);

}