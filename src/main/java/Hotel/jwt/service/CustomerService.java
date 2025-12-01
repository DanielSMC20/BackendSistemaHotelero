package Hotel.jwt.service;

import Hotel.jwt.dto.customer.CustomerRequest;
import Hotel.jwt.entity.Clientes;

import java.util.List;

public interface CustomerService {
    Clientes create(Clientes c);
    List<Clientes> searchByName(String name);
    List<Clientes> findAll();
    Clientes findByDocumento(String documento);
    Clientes updateByDocumento(String documento, Clientes c);

    Clientes createFromRequest(CustomerRequest req);
}
