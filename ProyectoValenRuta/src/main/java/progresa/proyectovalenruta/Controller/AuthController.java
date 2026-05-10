package progresa.proyectovalenruta.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import progresa.proyectovalenruta.DTO.LoginRequest;
import progresa.proyectovalenruta.DTO.LoginResponse;
import progresa.proyectovalenruta.DTO.RegisterRequestDTO;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Security.JwtUtil;
import progresa.proyectovalenruta.Service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // LOGIN CON BCrypt
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){

        Usuario usuario = usuarioService.findByEmail(request.getEmail());

        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuario no encontrado");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(usuario.getEmail());

        LoginResponse resp = new LoginResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail()
        );

        return ResponseEntity.ok(resp);
    }


    @GetMapping("/ping")
    public String ping() {
        return "AUTH FUNCIONA";
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {

        Usuario usuario = usuarioService.register(
                request.getNombre(),
                request.getEmail(),
                request.getPassword(),
                request.getTelefono()
        );

        return ResponseEntity.ok(usuario);
    }
}
