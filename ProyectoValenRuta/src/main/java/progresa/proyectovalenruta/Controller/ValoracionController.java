package progresa.proyectovalenruta.Controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import progresa.proyectovalenruta.DTO.CrearValoracionDTO;
import progresa.proyectovalenruta.DTO.ValoracionResponseDTO;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Service.UsuarioService;
import progresa.proyectovalenruta.Service.ValoracionService;

import java.util.List;

@RestController
@RequestMapping("/api/valoraciones")
@CrossOrigin("*")
public class ValoracionController {

    @Autowired
    private ValoracionService valoracionService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ValoracionResponseDTO crearValoracion(@Valid @RequestBody CrearValoracionDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioService.findByEmail(email);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado");
        }

        return valoracionService.crearValoracion(dto, usuario.getId());
    }

    @GetMapping("/conductor/{id}")
    public List<ValoracionResponseDTO> getValoracionesPorConductor(@PathVariable Long id) {
        return valoracionService.getValoracionesPorConductor(id);
    }

    @GetMapping("/usuario/{id}")
    public List<ValoracionResponseDTO> getValoracionesPorUsuario(@PathVariable Long id) {
        return valoracionService.getValoracionesPorUsuario(id);
    }
}
