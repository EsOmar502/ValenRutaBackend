package progresa.proyectovalenruta.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DTO.ConversacionDTO;
import progresa.proyectovalenruta.DTO.MensajeDTO;
import progresa.proyectovalenruta.DTO.MensajeResponseDTO;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Service.MensajeService;
import progresa.proyectovalenruta.Service.UsuarioService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mensajes")
@CrossOrigin("*")
public class MensajeController {

    @Autowired
    private MensajeService mensajeService;

    @Autowired
    private UsuarioService usuarioService;

    // ── Fixed routes FIRST ──────────────────────

    // GET conversations list
    @GetMapping("/conversaciones")
    public List<ConversacionDTO> getConversaciones() {

        Usuario usuario = getAuthenticatedUser();
        return mensajeService.getConversaciones(usuario.getId());
    }

    // GET unread count (for badge)
    @GetMapping("/no-leidos")
    public Map<String, Integer> getNoLeidos() {

        Usuario usuario = getAuthenticatedUser();
        int count = mensajeService.countNoLeidos(usuario.getId());
        return Map.of("noLeidos", count);
    }

    // POST send message
    @PostMapping
    public MensajeResponseDTO crear(@RequestBody MensajeDTO dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return mensajeService.enviarMensaje(
                email,
                dto.getReceptorId(),
                dto.getContenido()
        );
    }

    // ── Path variable routes AFTER ──────────────

    // GET conversation with specific user
    @GetMapping("/conversacion/{usuarioId}")
    public List<MensajeResponseDTO> getConversacion(@PathVariable Long usuarioId) {

        Usuario usuario = getAuthenticatedUser();
        return mensajeService.getConversacion(usuario.getId(), usuarioId);
    }

    // ── Helper ──────────────────────────────────
    private Usuario getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioService.findByEmail(email);

        if (usuario == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no encontrado"
            );
        }

        return usuario;
    }
}
