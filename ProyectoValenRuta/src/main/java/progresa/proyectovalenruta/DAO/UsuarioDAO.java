package progresa.proyectovalenruta.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import progresa.proyectovalenruta.Entity.Usuario;

import java.util.Optional;

public interface UsuarioDAO extends JpaRepository<Usuario, Long> {


    Optional<Usuario> findByEmail(String email);
}
