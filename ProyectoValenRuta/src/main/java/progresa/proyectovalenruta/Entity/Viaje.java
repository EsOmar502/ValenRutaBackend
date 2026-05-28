package progresa.proyectovalenruta.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "viajes",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"origen", "destino", "fecha_salida", "conductor_id"})})
public class Viaje {

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "viaje")
    private List<Reserva> reservas;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El origen es obligatorio")
    private String origen;

    @NotBlank(message = "El destino es obligatorio")
    private String destino;

    @NotNull(message = "La latitud de origen es obligatoria")
    private Double latOrigen;

    @NotNull(message = "La longitud de origen es obligatoria")
    private Double lngOrigen;

    @NotNull(message = "La latitud de destino es obligatoria")
    private Double latDestino;

    @NotNull(message = "La longitud de destino es obligatoria")
    private Double lngDestino;

    @NotNull(message = "La fecha de salida es obligatoria")
    private LocalDateTime fechaSalida;

    @Enumerated(EnumType.STRING)
    private EstadoViaje estado;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
    private Double precio;

    @NotNull(message = "Los asientos disponibles son obligatorios")
    @Min(value = 0, message = "Los asientos no pueden ser negativos")
    @Max(value = 4, message = "Máximo 4 asientos")
    private Integer asientosDisponibles;

    @ManyToOne
    @JoinColumn(name = "conductor_id", nullable = false)
    private Conductor conductor;
}