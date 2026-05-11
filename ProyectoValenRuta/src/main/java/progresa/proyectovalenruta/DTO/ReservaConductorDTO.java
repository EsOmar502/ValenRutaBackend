package progresa.proyectovalenruta.DTO;

import lombok.Data;

@Data
public class ReservaConductorDTO {

    private Long reservaId;

    private String usuarioNombre;
    private String usuarioEmail;

    private int asientos;

    private String origen;
    private String destino;
    private String fechaSalida;

    private String estadoViaje;

}