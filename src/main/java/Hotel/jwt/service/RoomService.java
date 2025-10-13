package Hotel.jwt.service;

import Hotel.jwt.entity.Habitacion;

import java.util.List;

public interface RoomService {
    Habitacion create(Habitacion r);
    List<Habitacion> listAvailable();
}