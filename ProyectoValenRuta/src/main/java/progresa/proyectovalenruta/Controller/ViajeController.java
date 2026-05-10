package progresa.proyectovalenruta.Controller;

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

import java.time.LocalDate;
import java.util.List;

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


    @GetMapping("/{id}")
    public ViajeDetalleDTO getById(@PathVariable Long id) {

        return viajeService.getDetalleViaje(id);
    }

    // 🔓 GET
    @GetMapping
    public List<Viaje> listar() {
        return viajeService.getAll();
    }

    // 🔒 CREATE (mantengo tu lógica actual)
    @PostMapping
    public Viaje crear(@RequestBody ViajeDTO dto) {

        // 🔐 1. Usuario desde JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioService.findByEmail(email);

        if (usuario == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no encontrado"
            );
        }

        // 🔥 2. Obtener conductor del usuario
        Conductor conductor = conductorService.getByUsuarioId(usuario.getId());

        // 🔒 3. Validar verificación
        if (!conductor.isVerificado()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El conductor no está verificado"
            );
        }

        // 🧾 4. Validaciones
        if (dto.getAsientosDisponibles() == null || dto.getAsientosDisponibles() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los asientos deben ser mayores que 0");
        }

        if (dto.getPrecio() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor que 0");
        }

        // 🚗 5. Crear viaje
        Viaje v = new Viaje();
        v.setOrigen(dto.getOrigen());
        v.setDestino(dto.getDestino());
        v.setFecha(dto.getFecha());
        v.setPrecio(dto.getPrecio());
        v.setAsientosDisponibles(dto.getAsientosDisponibles());
        v.setConductor(conductor);

        v.setOrigenLat(dto.getOrigenLat());
        v.setOrigenLng(dto.getOrigenLng());
        v.setDestinoLat(dto.getDestinoLat());
        v.setDestinoLng(dto.getDestinoLng());

        return viajeService.save(v);
    }

    // 🔒 DELETE seguro
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

    // 🔒 PUT (CORREGIDO)
    @PutMapping("/{id}")
    public Viaje actualizar(@PathVariable Long id, @RequestBody ViajeDTO dto) {

        Viaje existente = viajeService.getById(id);

        if (existente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado");
        }

        // 🔐 VALIDAR PROPIETARIO
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        if (!existente.getConductor().getUsuario().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        // VALIDACIONES
        if (dto.getAsientosDisponibles() == null || dto.getAsientosDisponibles() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los asientos deben ser mayores que 0");
        }

        if (dto.getPrecio() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor que 0");
        }

        // 🔥 IMPORTANTE: NO CAMBIAR CONDUCTOR
        existente.setOrigen(dto.getOrigen());
        existente.setDestino(dto.getDestino());
        existente.setFecha(dto.getFecha());
        existente.setPrecio(dto.getPrecio());
        existente.setAsientosDisponibles(dto.getAsientosDisponibles());

        return viajeService.save(existente);
    }

    // 🔒 PATCH (CORREGIDO)
    @PatchMapping("/{id}")
    public Viaje actualizarParcial(@PathVariable Long id, @RequestBody ViajeDTO dto) {

        Viaje existente = viajeService.getById(id);

        if (existente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado");
        }

        // 🔐 VALIDAR PROPIETARIO
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

        if (dto.getFecha() != null) {
            existente.setFecha(dto.getFecha());
        }

        if (dto.getPrecio() != 0) {
            if (dto.getPrecio() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor que 0");
            }
            existente.setPrecio(dto.getPrecio());
        }

        if (dto.getAsientosDisponibles() != null) {
            if (dto.getAsientosDisponibles() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los asientos deben ser mayores que 0");
            }
            existente.setAsientosDisponibles(dto.getAsientosDisponibles());
        }

        return viajeService.save(existente);
    }


    @GetMapping("/disponibles")
    public List<ViajeDisponibleDTO> buscarDisponibles(
            @RequestParam(required = false) String origen,
            @RequestParam(required = false) String destino,
            @RequestParam(required = false) String fecha
    ) {

        return viajeService.getViajesDisponibles();
    }


    @PatchMapping("/finalizar/{id}")
    public Viaje finalizar(@PathVariable Long id) {
        return viajeService.finalizarViaje(id);
    }

    @GetMapping("/cercanos")
    public List<ViajeCercanoDTO> buscarCercanos(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam Double radio
    ) {
        return viajeService.buscarCercanos(lat, lng, radio);
    }
}