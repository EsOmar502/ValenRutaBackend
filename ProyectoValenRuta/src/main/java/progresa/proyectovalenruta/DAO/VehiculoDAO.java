package progresa.proyectovalenruta.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import progresa.proyectovalenruta.Entity.Vehiculo;


import java.util.Optional;

public interface VehiculoDAO extends JpaRepository<Vehiculo, Long> {

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndIdNot(String placa, Long id);

    Optional<Vehiculo> findByUsuario_Id(Long usuarioId);
}