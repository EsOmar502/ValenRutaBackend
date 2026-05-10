package progresa.proyectovalenruta.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservaDTO {

    @NotNull
    @Min(value = 1, message = "Debe reservar al menos 1 asiento")
    private Integer asientosReservados;

    @NotNull(message = "El viaje es obligatorio")
    private Long viajeId;
}