// Controller
package Hotel.jwt.controller;

import Hotel.jwt.entity.PhoneCountryCode;
import Hotel.jwt.service.PhoneCountryCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/phone-codes")
public class PhoneCountryCodeController {
    private final PhoneCountryCodeService service;

    @GetMapping
    public ResponseEntity<List<PhoneCountryCode>> list() {
        return ResponseEntity.ok(service.listAll());
    }
}
