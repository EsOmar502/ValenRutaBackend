package progresa.proyectovalenruta.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import progresa.proyectovalenruta.DTO.LoginDTO;
import progresa.proyectovalenruta.DTO.UsuarioDTO;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin("*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.getAll();
    }

    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable Long id) {
        return usuarioService.getById(id);
    }

    @PostMapping
    public Usuario crear(@RequestBody UsuarioDTO dto) {
        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setEmail(dto.getEmail());
        u.setTelefono(dto.getTelefono());
        u.setPassword(dto.getPassword());
        return usuarioService.save(u);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody UsuarioDTO dto) {
        Usuario u = usuarioService.getById(id);
        if (u == null) return null;

        u.setNombre(dto.getNombre());
        u.setEmail(dto.getEmail());
        u.setTelefono(dto.getTelefono());
        u.setPassword(dto.getPassword());

        return usuarioService.save(u);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        usuarioService.delete(id);
    }

    @PostMapping("/login")
    public Usuario login(@RequestBody LoginDTO loginDTO) {
        Usuario u = usuarioService.findByEmail(loginDTO.getEmail());

        if (u == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        if (!u.getPassword().equals(loginDTO.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return u;
    }




}
