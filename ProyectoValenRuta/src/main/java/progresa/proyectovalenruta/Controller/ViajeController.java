package progresa.proyectovalenruta.Controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DTO.ViajeDTO;
import progresa.proyectovalenruta.DTO.ViajeDetalleDTO;
import progresa.proyectovalenruta.DTO.ViajeDisponibleDTO;
import progresa.proyectovalenruta.Entity.Conductor;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Viaje;
import progresa.proyectovalenruta.Service.ConductorService;
import progresa.proyectovalenruta.Service.UsuarioService;
import progresa.proyectovalenruta.Service.ViajeService;
import progresa.proyectovalenruta.DTO.ViajeCercanoDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/viajes")
@CrossOrigin("*")
public class ViajeController {

    @Autowired
    private ViajeService viajeService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ConductorService conductorService;

    // ═══════════════════════════════════════════════
    // RUTAS FIJAS (ANTES de /{id} para evitar conflictos)
    // ═══════════════════════════════════════════════

    // GET todos (devuelve DTOs para evitar loops Jackson)
    @GetMapping
    public List<ViajeDisponibleDTO> listar() {
        return viajeService.getViajesDisponibles();
    }

    // GET mis viajes publicados como conductor
    @GetMapping("/mis")
    public List<ViajeDisponibleDTO> misViajes() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioService.findByEmail(email);

        if (usuario == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no encontrado"
            );
        }

        return viajeService.getMisViajes(usuario.getId());
    }

    // GET viajes disponibles
    @GetMapping("/disponibles")
    public List<ViajeDisponibleDTO> buscarDisponibles(
            @RequestParam(required = false) String origen,
            @RequestParam(required = false) String destino,
            @RequestParam(required = false) String fecha
    ) {

        return viajeService.getViajesDisponibles();
    }

    // GET viajes cercanos
    @GetMapping("/cercanos")
    public List<ViajeCercanoDTO> buscarCercanos(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam Double radio
    ) {
        return viajeService.buscarCercanos(lat, lng, radio);
    }

    // POST iniciar viaje
    @PostMapping("/{id}/iniciar")
    public Map<String, Object> iniciarViaje(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioService.findByEmail(auth.getName());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado");
        }
        Viaje viaje = viajeService.iniciarViaje(id, usuario.getId());
        return Map.of(
                "id", viaje.getId(),
                "estado", viaje.getEstado().name()
        );
    }

    // POST finalizar viaje
    @PostMapping("/{id}/finalizar")
    public Map<String, Object> finalizarViaje(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioService.findByEmail(auth.getName());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado");
        }
        Viaje viaje = viajeService.finalizarViaje(id, usuario.getId());
        return Map.of(
                "id", viaje.getId(),
                "estado", viaje.getEstado().name()
        );
    }

    // ═══════════════════════════════════════════════
    // RUTAS CON PATH VARIABLE (DESPUÉS de rutas fijas)
    // ═══════════════════════════════════════════════

    // GET por ID
    @GetMapping("/{id}")
    public ViajeDetalleDTO getById(@PathVariable Long id) {
        return viajeService.getDetalleViaje(id);
    }

    // POST crear viaje
    @PostMapping
    public Map<String, Object> crear(@Valid @RequestBody ViajeDTO dto) {

        // 1. Usuario desde JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioService.findByEmail(email);

        if (usuario == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no encontrado"
            );
        }

        // 2. Obtener conductor del usuario
        Conductor conductor = conductorService.getByUsuarioId(usuario.getId());

        // 3. Validar verificación
        if (!conductor.isVerificado()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El conductor no está verificado"
            );
        }

        // 4. Validaciones adicionales
        if (dto.getAsientosDisponibles() == null || dto.getAsientosDisponibles() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los asientos deben ser mayores que 0");
        }

        if (dto.getAsientosDisponibles() > 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Máximo 4 pasajeros por viaje");
        }

        if (dto.getPrecio() == null || dto.getPrecio() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor que 0");
        }

        if (dto.getFechaSalida() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de salida es obligatoria");
        }

        // 5. Crear viaje
        Viaje v = new Viaje();
        v.setOrigen(dto.getOrigen());
        v.setDestino(dto.getDestino());
        v.setFechaSalida(dto.getFechaSalida());
        v.setPrecio(dto.getPrecio());
        v.setAsientosDisponibles(dto.getAsientosDisponibles());
        v.setConductor(conductor);

        v.setLatOrigen(dto.getLatOrigen());
        v.setLngOrigen(dto.getLngOrigen());
        v.setLatDestino(dto.getLatDestino());
        v.setLngDestino(dto.getLngDestino());

        Viaje saved = viajeService.save(v);

        // Devolver DTO limpio en vez de entity
        return Map.of(
                "id", saved.getId(),
                "origen", saved.getOrigen(),
                "destino", saved.getDestino(),
                "estado", saved.getEstado() != null ? saved.getEstado().name() : null,
                "precio", saved.getPrecio(),
                "asientosDisponibles", saved.getAsientosDisponibles()
        );
    }

    // DELETE seguro
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Viaje viaje = viajeService.getById(id);

        if (viaje == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado");
        }

        if (!viaje.getConductor().getUsuario().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        viajeService.delete(id);
    }

    // PUT actualización completa
    @PutMapping("/{id}")
    public Map<String, Object> actualizar(@PathVariable Long id, @Valid @RequestBody ViajeDTO dto) {

        Viaje existente = viajeService.getById(id);

        if (existente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado");
        }

        // VALIDAR PROPIETARIO
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        if (!existente.getConductor().getUsuario().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        // VALIDACIONES
        if (dto.getAsientosDisponibles() == null || dto.getAsientosDisponibles() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los asientos deben ser mayores que 0");
        }

        if (dto.getAsientosDisponibles() > 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Máximo 4 pasajeros por viaje");
        }

        if (dto.getPrecio() == null || dto.getPrecio() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor que 0");
        }

        // NO CAMBIAR CONDUCTOR
        existente.setOrigen(dto.getOrigen());
        existente.setDestino(dto.getDestino());
        existente.setFechaSalida(dto.getFechaSalida());
        existente.setPrecio(dto.getPrecio());
        existente.setAsientosDisponibles(dto.getAsientosDisponibles());

        existente.setLatOrigen(dto.getLatOrigen());
        existente.setLngOrigen(dto.getLngOrigen());
        existente.setLatDestino(dto.getLatDestino());
        existente.setLngDestino(dto.getLngDestino());

        Viaje saved = viajeService.update(existente);

        return Map.of(
                "id", saved.getId(),
                "origen", saved.getOrigen(),
                "destino", saved.getDestino(),
                "estado", saved.getEstado() != null ? saved.getEstado().name() : null,
                "precio", saved.getPrecio(),
                "asientosDisponibles", saved.getAsientosDisponibles()
        );
    }

    // PATCH parcial
    @PatchMapping("/{id}")
    public Map<String, Object> actualizarParcial(@PathVariable Long id, @RequestBody ViajeDTO dto) {

        Viaje existente = viajeService.getById(id);

        if (existente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado");
        }

        // VALIDAR PROPIETARIO
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        if (!existente.getConductor().getUsuario().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        if (dto.getOrigen() != null) {
            existente.setOrigen(dto.getOrigen());
        }

        if (dto.getDestino() != null) {
            existente.setDestino(dto.getDestino());
        }

        if (dto.getFechaSalida() != null) {
            existente.setFechaSalida(dto.getFechaSalida());
        }

        if (dto.getPrecio() != null) {
            if (dto.getPrecio() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor que 0");
            }
            existente.setPrecio(dto.getPrecio());
        }

        if (dto.getAsientosDisponibles() != null) {
            if (dto.getAsientosDisponibles() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los asientos deben ser mayores que 0");
            }
            if (dto.getAsientosDisponibles() > 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Máximo 4 pasajeros por viaje");
            }
            existente.setAsientosDisponibles(dto.getAsientosDisponibles());
        }

        if (dto.getLatOrigen() != null) existente.setLatOrigen(dto.getLatOrigen());
        if (dto.getLngOrigen() != null) existente.setLngOrigen(dto.getLngOrigen());
        if (dto.getLatDestino() != null) existente.setLatDestino(dto.getLatDestino());
        if (dto.getLngDestino() != null) existente.setLngDestino(dto.getLngDestino());

        Viaje saved = viajeService.update(existente);

        return Map.of(
                "id", saved.getId(),
                "origen", saved.getOrigen(),
                "destino", saved.getDestino(),
                "estado", saved.getEstado() != null ? saved.getEstado().name() : null,
                "precio", saved.getPrecio(),
                "asientosDisponibles", saved.getAsientosDisponibles()
        );
    }
}