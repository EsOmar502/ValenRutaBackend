package progresa.proyectovalenruta.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "viajes",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"origen","destino","conductor_id"})})
public class Viaje {

    @JsonIgnore
    @OneToMany(mappedBy = "viaje")
    private List<Reserva> reservas;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String origen;
    private String destino;
    private LocalDate fecha;
    private String estado;

    private double precio;
    private int asientosDisponibles;

    private Double origenLat;
    private Double origenLng;

    private Double destinoLat;
    private Double destinoLng;

    private LocalDateTime fechaSalida;
    private LocalDateTime fechaLlegadaEstimada;


    @ManyToOne
    @JoinColumn(name = "conductor_id", nullable = false)
    private Conductor conductor;
}