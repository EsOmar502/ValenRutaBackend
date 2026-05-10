package progresa.proyectovalenruta.Entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Data
@Entity
@Table(name = "conductores")
public class Conductor {

    @JsonIgnore
    @OneToMany(mappedBy = "conductor")
    private List<Viaje> viajes;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private Double rating = 0.0;

    private boolean verificado = false;
}