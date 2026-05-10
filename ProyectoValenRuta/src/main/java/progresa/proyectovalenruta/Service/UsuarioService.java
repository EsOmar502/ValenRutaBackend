package progresa.proyectovalenruta.Service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import progresa.proyectovalenruta.DAO.UsuarioDAO;
import progresa.proyectovalenruta.Entity.Usuario;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioDAO usuarioDAO;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioDAO usuarioDAO, PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> getAll() {
        return usuarioDAO.findAll();
    }

    public Usuario getById(Long id) {
        return usuarioDAO.findById(id).orElse(null);
    }

    public Usuario save(Usuario usuario) {
        return usuarioDAO.save(usuario);
    }

    public void delete(Long id) {
        usuarioDAO.deleteById(id);
    }

    public Usuario findByEmail(String email) {
        return usuarioDAO.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }



    public Usuario register(String nombre, String email, String password, String telefono) {

        if (usuarioDAO.findByEmail(email).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password)); // 🔐 Encriptado correctamente
        usuario.setTelefono(telefono);

        return usuarioDAO.save(usuario);
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioDAO.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado"));

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

    }
}
