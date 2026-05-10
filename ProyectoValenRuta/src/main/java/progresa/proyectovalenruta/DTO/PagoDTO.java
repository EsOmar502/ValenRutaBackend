package progresa.proyectovalenruta.DTO;

import lombok.Data;

@Data
public class PagoDTO {
    private double monto;
    private String fecha;
    private Long usuarioId;
    private Long viajeId;
}
