package progresa.proyectovalenruta.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import progresa.proyectovalenruta.Entity.Viaje;


import java.time.LocalDate;
import java.util.List;

public interface ViajeDAO extends JpaRepository<Viaje, Long> {

    boolean existsByOrigenAndDestinoAndFechaAndConductor_Id(
            String origen,
            String destino,
            LocalDate fecha,
            Long conductorId
    );

    // 🔥 NUEVO
    @Query("""
        SELECT v FROM Viaje v
        WHERE v.asientosDisponibles > 0
        AND (:origen IS NULL OR LOWER(v.origen) LIKE LOWER(CONCAT('%', :origen, '%')))
        AND (:destino IS NULL OR LOWER(v.destino) LIKE LOWER(CONCAT('%', :destino, '%')))
        AND (:fecha IS NULL OR v.fecha = :fecha)
    """)
    List<Viaje> buscarDisponibles(
            String origen,
            String destino,
            LocalDate fecha
    );
}