package progresa.proyectovalenruta.DTO;

import lombok.Data;

@Data
public class ReservaResponseDTO {

    private Long id;
    private int asientosReservados;

    private String usuarioNombre;
    private String usuarioEmail;

    private String conductorNombre;
    private Long conductorId;

    private String origen;
    private String destino;
    private String fechaSalida;
    private double precio;
}