package progresa.proyectovalenruta.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DTO.ValoracionDTO;
import progresa.proyectovalenruta.Entity.Conductor;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Valoracion;
import progresa.proyectovalenruta.Entity.Viaje;
import progresa.proyectovalenruta.Service.ValoracionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/valoraciones")
@CrossOrigin("*")
public class ValoracionController {

    @Autowired
    private ValoracionService valoracionService;


    @GetMapping
    public List<Valoracion> listar() {
        return valoracionService.getAll();
    }


    @PostMapping
    public Valoracion crear(
            @RequestBody ValoracionDTO dto,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return valoracionService.crearValoracionDesdeToken(
                email,
                dto.getEvaluadoId(),
                dto.getViajeId(),
                dto.getPuntuacion(),
                dto.getComentario()
        );
    }

    @PatchMapping("/{id}/comentario")
    public Valoracion actualizarComentario(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Valoracion valoracion = valoracionService.getById(id);

        if (valoracion == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Valoración no encontrada"
            );
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        if (!valoracion.getEvaluador()
                .getEmail()
                .equalsIgnoreCase(email)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No autorizado"
            );
        }

        String nuevoComentario = body.get("comentario");

        if (nuevoComentario == null || nuevoComentario.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Comentario inválido"
            );
        }

        valoracion.setComentario(nuevoComentario);

        return valoracionService.save(valoracion);
    }

    @DeleteMapping("/{id}/comentario")
    public Valoracion eliminarComentario(@PathVariable Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return valoracionService.eliminarComentario(id, email);
    }


}
