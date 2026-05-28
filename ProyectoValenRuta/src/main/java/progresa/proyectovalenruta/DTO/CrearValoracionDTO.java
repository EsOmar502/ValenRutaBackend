package progresa.proyectovalenruta.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearValoracionDTO {

    @NotNull(message = "El ID del viaje es obligatorio")
    private Long viajeId;

    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    private int puntuacion;

    private String comentario;

    // Opcional para mantener compatibilidad con frontend anterior.
    // Si no se envía, se asume que el pasajero valora al conductor.
    private Long usuarioValoradoId;
}
