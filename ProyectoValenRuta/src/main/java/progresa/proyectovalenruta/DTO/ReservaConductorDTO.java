package progresa.proyectovalenruta.DTO;

import lombok.Data;

@Data
public class ReservaConductorDTO {

    private Long reservaId;
    private Long viajeId;
    private Long usuarioId;

    private String usuarioNombre;
    private String usuarioEmail;
    private Double usuarioRating;

    private int asientos;

    private String origen;
    private String destino;
    private String fechaSalida;

    private String estadoViaje;
    private String estadoReserva;
}
