package Hotel.jwt.service.serviceImpl;
import Hotel.jwt.entity.PhoneCountryCode;
import Hotel.jwt.repository.PhoneCountryCodeRepository;
import Hotel.jwt.service.PhoneCountryCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class PhoneCountryCodeServiceImpl implements PhoneCountryCodeService {
    private final PhoneCountryCodeRepository repo;
    @Override public List<PhoneCountryCode> listAll() { return repo.findAll(); }
}