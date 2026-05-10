package progresa.proyectovalenruta.DTO;

import lombok.Data;

@Data
public class ViajeCercanoDTO {

    private Long id;
    private String origen;
    private String destino;
    private double precio;
    private double distanciaKm;
}