
package progresa.proyectovalenruta.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Viaje;

@Entity
@Getter
@Setter
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Max(value = 5, message = "La puntuación máxima es 5")
    private int puntuacion;

    private String comentario;

    @ManyToOne
    @JoinColumn(name = "usuario_que_valora_id", nullable = false)
    private Usuario usuarioQueValora;

    @ManyToOne
    @JoinColumn(name = "usuario_valorado_id", nullable = false)
    private Usuario usuarioValorado;

    @ManyToOne
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
