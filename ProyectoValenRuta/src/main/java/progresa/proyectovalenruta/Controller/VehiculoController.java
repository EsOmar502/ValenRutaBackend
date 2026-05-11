package progresa.proyectovalenruta.Controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Vehiculo;
import progresa.proyectovalenruta.Service.UsuarioService;
import progresa.proyectovalenruta.Service.VehiculoService;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@CrossOrigin("*")
public class VehiculoController {

    @Autowired
    private VehiculoService vehiculoService;

    @Autowired
    private UsuarioService usuarioService;

    // 🔓 GET público
    @GetMapping
    public List<Vehiculo> listar() {
        return vehiculoService.getAll();
    }

    // 🔓 GET por ID público
    @GetMapping("/{id}")
    public Vehiculo obtener(@PathVariable Long id) {

        Vehiculo vehiculo = vehiculoService.getById(id);

        if (vehiculo == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Vehículo no encontrado"
            );
        }

        return vehiculo;
    }

    // 🔒 POST protegido (usuario desde JWT + VALIDACIÓN ACTIVA)
    @PostMapping
    public Vehiculo crear(@Valid @RequestBody Vehiculo vehiculo) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioService.findByEmail(email);

        if (usuario == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no encontrado"
            );
        }

        // 🔥 IMPORTANTE: ignoramos el usuario del body
        vehiculo.setUsuario(usuario);

        return vehiculoService.save(vehiculo);
    }

    // 🔒 PUT completo (solo dueño)
    @PutMapping("/{id}")
    public Vehiculo actualizar(@PathVariable Long id,
                               @Valid @RequestBody Vehiculo vehiculo) {

        Vehiculo existente = vehiculoService.getById(id);

        if (existente == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Vehículo no encontrado"
            );
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        if (!existente.getUsuario().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No autorizado"
            );
        }

        existente.setMarca(vehiculo.getMarca());
        existente.setModelo(vehiculo.getModelo());
        existente.setPlaca(vehiculo.getPlaca());
        existente.setColor(vehiculo.getColor());

        return vehiculoService.save(existente);
    }

    @GetMapping("/mis-vehiculos")
    public Vehiculo miVehiculo(Authentication auth) {

        String email = auth.getName();

        Usuario usuario = usuarioService.findByEmail(email);

        if (usuario == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no encontrado"
            );
        }

        System.out.println("EMAIL TOKEN: " + auth.getName());
        System.out.println("USUARIO ID: " + usuario.getId());

        Vehiculo vehiculo = vehiculoService.getByUsuario(usuario.getId());

        if (vehiculo == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No tiene vehículo"
            );
        }

        return vehiculo;
    }

    // 🔒 PATCH parcial
    @PatchMapping("/{id}")
    public Vehiculo actualizarParcial(@PathVariable Long id,
                                      @RequestBody Vehiculo vehiculo) {

        Vehiculo existente = vehiculoService.getById(id);

        if (existente == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Vehículo no encontrado"
            );
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        if (!existente.getUsuario().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No autorizado"
            );
        }

        if (vehiculo.getMarca() != null)
            existente.setMarca(vehiculo.getMarca());

        if (vehiculo.getModelo() != null)
            existente.setModelo(vehiculo.getModelo());

        if (vehiculo.getPlaca() != null)
            existente.setPlaca(vehiculo.getPlaca());

        if (vehiculo.getColor() != null)
            existente.setColor(vehiculo.getColor());

        return vehiculoService.save(existente);
    }

    // 🔒 DELETE
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {

        Vehiculo vehiculo = vehiculoService.getById(id);

        if (vehiculo == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Vehículo no encontrado"
            );
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        if (!vehiculo.getUsuario().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No autorizado"
            );
        }

        vehiculoService.delete(id);
    }
}