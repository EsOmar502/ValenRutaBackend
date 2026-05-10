package progresa.proyectovalenruta.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Valoracion;
import progresa.proyectovalenruta.Entity.Viaje;

import java.util.List;

public interface ValoracionDAO extends JpaRepository<Valoracion, Long> {

    List<Valoracion> findByEvaluado(Usuario evaluado);

    List<Valoracion> findByEvaluador(Usuario evaluador);

    boolean existsByEvaluadorAndEvaluadoAndViaje(
            Usuario evaluador,
            Usuario evaluado,
            Viaje viaje
    );
}
