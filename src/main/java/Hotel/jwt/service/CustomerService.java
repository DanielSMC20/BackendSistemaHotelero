package Hotel.jwt.service;

import Hotel.jwt.entity.Clientes;

import java.util.List;

public interface CustomerService {
    Clientes create(Clientes c);
    List<Clientes> searchByName(String name);
}
