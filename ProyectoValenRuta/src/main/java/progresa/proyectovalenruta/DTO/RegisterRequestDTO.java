package progresa.proyectovalenruta.DTO;

import lombok.Data;

@Data
public class RegisterRequestDTO {

    private String nombre;
    private String email;
    private String password;
    private String telefono;
}
