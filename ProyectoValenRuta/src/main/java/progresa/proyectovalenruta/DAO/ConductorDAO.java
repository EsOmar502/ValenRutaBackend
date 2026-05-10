package progresa.proyectovalenruta.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import progresa.proyectovalenruta.Entity.Conductor;
import progresa.proyectovalenruta.Entity.Usuario;

import java.util.Optional;

public interface ConductorDAO extends JpaRepository<Conductor, Long> {

    // 🔥 Buscar por objeto Usuario (el que ya tienes)
    Conductor findByUsuario(Usuario usuario);

    // 🔥 MUY IMPORTANTE (más robusto)
    Optional<Conductor> findByUsuarioId(Long usuarioId);

    // 🔥 Opcional (útil para validaciones rápidas)
    boolean existsByUsuarioId(Long usuarioId);
}