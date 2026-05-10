package progresa.proyectovalenruta.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ViajeDTO {

    private String origen;
    private String destino;
    private LocalDate fecha;
    private double precio;
    private Integer asientosDisponibles;

    private Double origenLat;
    private Double origenLng;
    private Double destinoLat;
    private Double destinoLng;
}