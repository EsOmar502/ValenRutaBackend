package progresa.proyectovalenruta.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import progresa.proyectovalenruta.DTO.MensajeDTO;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Mensaje;
import progresa.proyectovalenruta.Service.UsuarioService;
import progresa.proyectovalenruta.Service.MensajeService;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
@CrossOrigin("*")
public class MensajeController {

    @Autowired
    private MensajeService mensajeService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Mensaje> listar(Authentication authentication) {

        String email = authentication.getName();

        return mensajeService.getMensajesUsuario(email);
    }

    @PostMapping
    public Mensaje crear(@RequestBody MensajeDTO dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        System.out.println("RECEPTOR ID: " + dto.getReceptorId());

        return mensajeService.enviarMensaje(
                email,
                dto.getReceptorId(),
                dto.getContenido()
        );


    }
}
