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

    @GetMapping
    public List<ReservaResponseDTO> listar() {
        return reservaService.getAll().stream().map(r -> {
            ReservaResponseDTO dto = new ReservaResponseDTO();

            dto.setId(r.getId());
            dto.setAsientosReservados(r.getAsientosReservados());

            dto.setUsuarioNombre(r.getUsuario().getNombre());
            dto.setUsuarioEmail(r.getUsuario().getEmail());
            dto.setConductorNombre(r.getViaje().getConductor().getUsuario().getNombre());

            dto.setOrigen(r.getViaje().getOrigen());
            dto.setDestino(r.getViaje().getDestino());
            dto.setFechaSalida(r.getViaje().getFechaSalida() != null ? r.getViaje().getFechaSalida().toString() : null);
            dto.setPrecio(r.getViaje().getPrecio());

            return dto;
        }).toList();
    }

    @GetMapping("/mis")
    public List<ReservaResponseDTO> misReservas() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = Optional.ofNullable(usuarioService.findByEmail(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                ));

        return reservaService.getByUsuarioId(usuario.getId()).stream().map(r -> {
            ReservaResponseDTO dto = new ReservaResponseDTO();

            dto.setId(r.getId());
            dto.setAsientosReservados(r.getAsientosReservados());

            dto.setUsuarioNombre(r.getUsuario().getNombre());
            dto.setUsuarioEmail(r.getUsuario().getEmail());
            dto.setConductorNombre(r.getViaje().getConductor().getUsuario().getNombre());

            dto.setOrigen(r.getViaje().getOrigen());
            dto.setDestino(r.getViaje().getDestino());
            dto.setFechaSalida(r.getViaje().getFechaSalida() != null ? r.getViaje().getFechaSalida().toString() : null);
            dto.setPrecio(r.getViaje().getPrecio());

            return dto;
        }).toList();
    }

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

        ReservaResponseDTO response = new ReservaResponseDTO();
        response.setId(reserva.getId());
        response.setAsientosReservados(reserva.getAsientosReservados());

        response.setUsuarioNombre(reserva.getUsuario().getNombre());
        response.setUsuarioEmail(reserva.getUsuario().getEmail());
        response.setConductorNombre(reserva.getViaje().getConductor().getUsuario().getNombre());

        response.setOrigen(reserva.getViaje().getOrigen());
        response.setDestino(reserva.getViaje().getDestino());
        response.setFechaSalida(reserva.getViaje().getFechaSalida() != null ? reserva.getViaje().getFechaSalida().toString() : null);
        response.setPrecio(reserva.getViaje().getPrecio());

        return response;
    }

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

        // 🔥 ACTUALIZAR
        Reserva reserva = reservaService.actualizarReserva(existente, dto);

        // 🔥 MAPEAR A DTO
        ReservaResponseDTO response = new ReservaResponseDTO();

        response.setId(reserva.getId());
        response.setAsientosReservados(reserva.getAsientosReservados());

        response.setUsuarioNombre(reserva.getUsuario().getNombre());
        response.setUsuarioEmail(reserva.getUsuario().getEmail());

        response.setOrigen(reserva.getViaje().getOrigen());
        response.setDestino(reserva.getViaje().getDestino());
        response.setFechaSalida(reserva.getViaje().getFechaSalida() != null ? reserva.getViaje().getFechaSalida().toString() : null);
        response.setPrecio(reserva.getViaje().getPrecio());

        return response;
    }
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
                .map(r -> {
                    ReservaResponseDTO dto = new ReservaResponseDTO();

                    dto.setId(r.getId());
                    dto.setAsientosReservados(r.getAsientosReservados());

                    dto.setUsuarioNombre(r.getUsuario().getNombre());
                    dto.setUsuarioEmail(r.getUsuario().getEmail());
                    dto.setConductorNombre(r.getViaje().getConductor().getUsuario().getNombre());

                    dto.setOrigen(r.getViaje().getOrigen());
                    dto.setDestino(r.getViaje().getDestino());
                    dto.setFechaSalida(r.getViaje().getFechaSalida() != null ? r.getViaje().getFechaSalida().toString() : null);
                    dto.setPrecio(r.getViaje().getPrecio());

                    return dto;
                })
                .toList();
    }

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

    @PatchMapping("/cancelar/{id}")
    public Reserva cancelar(@PathVariable Long id) {
        return reservaService.cancelarReserva(id);
    }
}