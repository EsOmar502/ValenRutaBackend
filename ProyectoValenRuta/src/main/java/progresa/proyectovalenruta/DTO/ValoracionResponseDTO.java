package progresa.proyectovalenruta.DTO;

import lombok.Data;

@Data
public class ValoracionResponseDTO {

    private String usuarioNombre;
    private int puntuacion;
    private String comentario;
    private String fecha;
    private Long usuarioValoradoId;
    private Long usuarioQueValoraId;
}
