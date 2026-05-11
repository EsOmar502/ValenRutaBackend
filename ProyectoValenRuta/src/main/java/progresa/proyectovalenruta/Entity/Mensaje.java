package progresa.proyectovalenruta.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String contenido;

    private LocalDateTime fecha;

    private boolean leido = false;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "emisor_id")
    private Usuario emisor;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "receptor_id")
    private Usuario receptor;
}
