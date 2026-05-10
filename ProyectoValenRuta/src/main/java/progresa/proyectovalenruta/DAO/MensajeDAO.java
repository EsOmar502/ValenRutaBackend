package progresa.proyectovalenruta.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import progresa.proyectovalenruta.Entity.Mensaje;

import java.util.List;

public interface MensajeDAO extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByEmisor_EmailOrReceptor_EmailOrderByFechaAsc(
            String emisorEmail,
            String receptorEmail
    );

}
