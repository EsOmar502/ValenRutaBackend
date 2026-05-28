package progresa.proyectovalenruta.Controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DTO.ReservaConductorDTO;
import progresa.proyectovalenruta.DTO.ReservaDTO;
import progresa.proyectovalenruta.DTO.ReservaResponseDTO;
import progresa.proyectovalenruta.Entity.Reserva;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Viaje;
import progresa.proyectovalenruta.Service.ReservaService;
import progresa.proyectovalenruta.Service.UsuarioService;
import progresa.proyectovalenruta.Service.ViajeService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin("*")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ViajeService viajeService;

    @Autowired
    private UsuarioService usuarioService;

    // ═══════════════════════════════════════════════
    // LISTAR TODAS
    // ═══════════════════════════════════════════════
    @GetMapping
    public List<ReservaResponseDTO> listar() {
        return reservaService.getAll().stream()
                .map(reservaService::toResponseDTO)
                .toList();
    }

    // ═══════════════════════════════════════════════
    // MIS RESERVAS (TODAS del usuario)
    // ═══════════════════════════════════════════════
    @GetMapping("/mis")
    public List<ReservaResponseDTO> misReservas() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = Optional.ofNullable(usuarioService.findByEmail(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                ));

        return reservaService.getByUsuarioId(usuario.getId()).stream()
                .map(reservaService::toResponseDTO)
                .toList();
    }

    // ═══════════════════════════════════════════════
    // CREAR RESERVA
    // ═══════════════════════════════════════════════
    @PostMapping
    public ReservaResponseDTO crear(@Valid @RequestBody ReservaDTO dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = Optional.ofNullable(usuarioService.findByEmail(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                ));

        Viaje viaje = viajeService.getById(dto.getViajeId());

        if (viaje == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Viaje no encontrado"
            );
        }

        Reserva reserva = reservaService.crearReserva(
                usuario,
                viaje,
                dto.getAsientosReservados()
        );

        return reservaService.toResponseDTO(reserva);
    }

    // ═══════════════════════════════════════════════
    // ACTUALIZAR RESERVA
    // ═══════════════════════════════════════════════
    @PutMapping("/{id}")
    public ReservaResponseDTO actualizar(@PathVariable Long id,
                                         @Valid @RequestBody ReservaDTO dto) {

        Reserva existente = reservaService.getById(id);

        if (existente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        if (!existente.getUsuario().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        Reserva reserva = reservaService.actualizarReserva(existente, dto);
        return reservaService.toResponseDTO(reserva);
    }

    // ═══════════════════════════════════════════════
    // ELIMINAR RESERVA
    // ═══════════════════════════════════════════════
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {

        Reserva reserva = reservaService.getById(id);

        if (reserva == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        if (!reserva.getUsuario()
                .getEmail()
                .trim()
                .equalsIgnoreCase(email.trim())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No autorizado"
            );
        }
        reservaService.delete(id);
    }

    // ═══════════════════════════════════════════════
    // RESERVAS ACTIVAS (PENDIENTE, ACEPTADA, EN_CURSO)
    // ═══════════════════════════════════════════════
    @GetMapping("/activas")
    public List<ReservaResponseDTO> reservasActivas() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = Optional.ofNullable(usuarioService.findByEmail(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                ));

        return reservaService.getActivasByUsuario(usuario.getId())
                .stream()
                .map(reservaService::toResponseDTO)
                .toList();
    }

    // ═══════════════════════════════════════════════
    // VIAJES REALIZADOS (solo FINALIZADA)
    // ═══════════════════════════════════════════════
    @GetMapping("/finalizadas")
    public List<ReservaResponseDTO> reservasFinalizadas() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = Optional.ofNullable(usuarioService.findByEmail(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                ));

        return reservaService.getFinalizadasByUsuario(usuario.getId())
                .stream()
                .map(reservaService::toResponseDTO)
                .toList();
    }

    // ═══════════════════════════════════════════════
    // RESERVAS DE MIS VIAJES (para conductor)
    // ═══════════════════════════════════════════════
    @GetMapping("/conductor")
    public List<ReservaConductorDTO> reservasDeMisViajes() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = Optional.ofNullable(usuarioService.findByEmail(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                ));

        return reservaService.getReservasDeMisViajes(usuario.getId());
    }

    // ═══════════════════════════════════════════════
    // CANCELAR RESERVA (mantiene endpoint original)
    // ═══════════════════════════════════════════════
    @PatchMapping("/cancelar/{id}")
    public ReservaResponseDTO cancelar(@PathVariable Long id) {
        Usuario usuario = getAuthenticatedUser();
        Reserva reserva = reservaService.cancelarReserva(id, usuario.getId());
        return reservaService.toResponseDTO(reserva);
    }

    // ═══════════════════════════════════════════════
    // CAMBIOS DE ESTADO
    // ═══════════════════════════════════════════════

    @PostMapping("/{id}/aceptar")
    public ReservaResponseDTO aceptar(@PathVariable Long id) {
        Usuario usuario = getAuthenticatedUser();
        Reserva reserva = reservaService.aceptarReserva(id, usuario.getId());
        return reservaService.toResponseDTO(reserva);
    }

    @PostMapping("/{id}/iniciar")
    public ReservaResponseDTO iniciar(@PathVariable Long id) {
        Usuario usuario = getAuthenticatedUser();
        Reserva reserva = reservaService.iniciarReserva(id, usuario.getId());
        return reservaService.toResponseDTO(reserva);
    }

    @PostMapping("/{id}/finalizar")
    public ReservaResponseDTO finalizar(@PathVariable Long id) {
        Usuario usuario = getAuthenticatedUser();
        Reserva reserva = reservaService.finalizarReserva(id, usuario.getId());
        return reservaService.toResponseDTO(reserva);
    }

    @PostMapping("/{id}/cancelar")
    public ReservaResponseDTO cancelarPost(@PathVariable Long id) {
        Usuario usuario = getAuthenticatedUser();
        Reserva reserva = reservaService.cancelarReserva(id, usuario.getId());
        return reservaService.toResponseDTO(reserva);
    }

    private Usuario getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        try {
            return usuarioService.findByEmail(auth.getName());
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado");
        }
    }
}
