package Hotel.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "phone_country_codes",
        uniqueConstraints = @UniqueConstraint(name="uk_phonecodes_code", columnNames = "dial_code"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PhoneCountryCode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="country", length=80, nullable=false)
    private String country;            // Peru

    @Column(name="iso2", length=2, nullable=false)
    private String iso2;               // PE

    @Column(name="dial_code", length=6, nullable=false)
    private String dialCode;           // +51

    @Column(name="flag", length=8)
    private String flag;               // 🇵🇪 opcional
}
