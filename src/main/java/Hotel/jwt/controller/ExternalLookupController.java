package Hotel.jwt.controller;

import Hotel.jwt.service.external.ExternalLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/external")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowedOrigin}") // o configura CORS global
public class ExternalLookupController {

    private final ExternalLookupService svc;

    @GetMapping("/reniec-dni")
    public Map<String,Object> dni(@RequestParam String numero) {
        if (!numero.matches("\\d{8}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DNI inválido (8 dígitos)");
        return svc.dni(numero);
    }

    @GetMapping("/sunat-ruc")
    public Map<String,Object> ruc(@RequestParam String numero) {
        if (!numero.matches("\\d{11}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUC inválido (11 dígitos)");
        return svc.ruc(numero);
    }
}