package progresa.proyectovalenruta.DTO;

import lombok.Data;

@Data
public class ValoracionDTO {

    private Long evaluadoId;
    private Long viajeId;
    private int puntuacion;
    private String comentario;
}

