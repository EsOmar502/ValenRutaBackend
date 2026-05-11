package progresa.proyectovalenruta.DTO;

import lombok.Data;

@Data
public class MensajeResponseDTO {

    private Long id;
    private String contenido;
    private String fecha;
    private boolean leido;

    private Long emisorId;
    private String emisorNombre;

    private Long receptorId;
    private String receptorNombre;
}
