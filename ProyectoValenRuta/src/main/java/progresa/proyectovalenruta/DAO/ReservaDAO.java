package progresa.proyectovalenruta.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import progresa.proyectovalenruta.Entity.Reserva;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Viaje;

import java.util.List;

public interface ReservaDAO extends JpaRepository<Reserva, Long> {

    boolean existsByUsuarioAndViaje(Usuario usuario, Viaje viaje);
    boolean existsByUsuario_IdAndViaje_Id(Long usuarioId, Long viajeId);
    boolean existsByUsuario_IdAndViaje_IdAndIdNot(Long usuarioId, Long viajeId, Long id);

    boolean existsByUsuario_IdAndViaje_IdAndEstadoNot(Long usuarioId, Long viajeId, String estado);
    List<Reserva> findByUsuario_IdAndEstado(Long usuarioId, String estado);

    List<Reserva> findByUsuario_Id(Long usuarioId);
}