package progresa.proyectovalenruta.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Data
@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario {

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "usuario")
    private List<Reserva> reservas;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String email;

    @JsonIgnore
    private String password;

    private String telefono;

    @ToString.Exclude
    @OneToOne(mappedBy = "usuario")
    private Conductor conductor;


}
