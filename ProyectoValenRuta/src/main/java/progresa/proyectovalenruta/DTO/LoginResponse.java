package progresa.proyectovalenruta.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long idUsuario;
    private String nombre;
    private String email;
}
