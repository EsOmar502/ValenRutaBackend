package progresa.proyectovalenruta.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import progresa.proyectovalenruta.Entity.Vehiculo;

public interface VehiculoDAO extends JpaRepository<Vehiculo, Long> {

    boolean existsByPlaca(String placa);
    boolean existsByPlacaAndIdNot(String placa, Long id);
}
