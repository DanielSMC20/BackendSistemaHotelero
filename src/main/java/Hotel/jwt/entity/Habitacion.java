package Hotel.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="habitaciones")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Habitacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="numero", nullable=false, unique=true, length=10)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name="tipo", nullable=false, length=20)
    private TipoHabitacion tipo;

    @Column(name="disponible")     private Boolean disponible;
    @Column(name="precio_por_noche") private Double precioPorNoche;
}