package progresa.proyectovalenruta.DTO;

import lombok.Data;

@Data
public class VehiculoDTO {
    private String marca;
    private String modelo;
    private String placa;
    private String color;
    private Long duenoId;
}
