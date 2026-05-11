package progresa.proyectovalenruta.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ViajeDTO {

    private String origen;
    private String destino;

    private Double latOrigen;
    private Double lngOrigen;
    private Double latDestino;
    private Double lngDestino;

    private LocalDateTime fechaSalida;

    private Double precio;
    private Integer asientosDisponibles;
}