package progresa.proyectovalenruta.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class Valoracion {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int puntuacion;

    private String comentario;

    @ManyToOne
    @JoinColumn(name = "evaluador_id", nullable = false)
    private Usuario evaluador;

    @ManyToOne
    @JoinColumn(name = "evaluado_id", nullable = false)
    private Usuario evaluado;

    @ManyToOne
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;
}
