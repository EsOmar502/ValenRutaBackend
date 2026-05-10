package progresa.proyectovalenruta.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import progresa.proyectovalenruta.DAO.UsuarioDAO;
import progresa.proyectovalenruta.DTO.LoginResponse;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Security.JwtUtil;

@Service
public class AuthService {

    private final UsuarioDAO usuarioDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioDAO usuarioDAO,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.usuarioDAO = usuarioDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(String email, String password) {

        Usuario usuario = usuarioDAO.findByEmail(email)
                .orElse(null);

        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(usuario.getEmail());

        return new LoginResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail()
        );
    }
}
