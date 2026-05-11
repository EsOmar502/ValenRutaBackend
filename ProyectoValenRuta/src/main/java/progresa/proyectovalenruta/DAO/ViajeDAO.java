package progresa.proyectovalenruta.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import progresa.proyectovalenruta.Entity.Viaje;

import java.time.LocalDateTime;
import java.util.List;

public interface ViajeDAO extends JpaRepository<Viaje, Long> {

    boolean existsByOrigenAndDestinoAndFechaSalidaAndConductor_Id(
            String origen,
            String destino,
            LocalDateTime fechaSalida,
            Long conductorId
    );

    @Query("""
        SELECT v FROM Viaje v
        WHERE v.asientosDisponibles > 0
        AND v.estado <> 'FINALIZADO'
        AND (:origen IS NULL OR LOWER(v.origen) LIKE LOWER(CONCAT('%', :origen, '%')))
        AND (:destino IS NULL OR LOWER(v.destino) LIKE LOWER(CONCAT('%', :destino, '%')))
        AND (:fechaSalida IS NULL OR v.fechaSalida >= :fechaSalida)
    """)
    List<Viaje> buscarDisponibles(
            String origen,
            String destino,
            LocalDateTime fechaSalida
    );
}