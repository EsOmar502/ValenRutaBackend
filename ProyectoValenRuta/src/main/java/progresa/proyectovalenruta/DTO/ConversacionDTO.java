package progresa.proyectovalenruta.DTO;

import lombok.Data;

@Data
public class ConversacionDTO {

    private Long usuarioId;
    private String usuarioNombre;
    private String iniciales;
    private String ultimoMensaje;
    private String fechaUltimoMensaje;
    private int noLeidos;
}
