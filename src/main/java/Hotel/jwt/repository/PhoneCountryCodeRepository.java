package Hotel.jwt.repository;
import Hotel.jwt.entity.PhoneCountryCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneCountryCodeRepository extends JpaRepository<PhoneCountryCode, Long> {
    Optional<PhoneCountryCode> findByIso2(String iso2);
    Optional<PhoneCountryCode> findByDialCode(String dialCode);
}
