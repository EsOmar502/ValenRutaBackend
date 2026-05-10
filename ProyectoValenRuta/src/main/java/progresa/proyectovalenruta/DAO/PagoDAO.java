package progresa.proyectovalenruta.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import progresa.proyectovalenruta.Entity.Pago;

public interface PagoDAO extends JpaRepository<Pago, Long> {
}
