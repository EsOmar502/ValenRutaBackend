package progresa.proyectovalenruta.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import progresa.proyectovalenruta.Entity.Valoracion;

import java.util.List;

public interface ValoracionDAO extends JpaRepository<Valoracion, Long> {

    List<Valoracion> findByUsuarioValorado_Id(Long usuarioValoradoId);

    List<Valoracion> findByUsuarioQueValora_Id(Long usuarioId);

    boolean existsByUsuarioQueValora_IdAndUsuarioValorado_IdAndViaje_Id(Long usuarioQueValoraId, Long usuarioValoradoId, Long viajeId);
}
