package progresa.proyectovalenruta.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DAO.UsuarioDAO;
import progresa.proyectovalenruta.DAO.VehiculoDAO;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Vehiculo;

import java.util.List;

@Service
public class VehiculoService {

    @Autowired
    private VehiculoDAO vehiculoDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    public List<Vehiculo> getAll() {
        return vehiculoDAO.findAll();
    }

    public Vehiculo getById(Long id) {
        return vehiculoDAO.findById(id).orElse(null);
    }

    public Vehiculo getByUsuario(Long usuarioId) {

        System.out.println("BUSCANDO VEHICULO PARA USUARIO: " + usuarioId);

        Vehiculo vehiculo = vehiculoDAO.findByUsuario_Id(usuarioId)
                .orElse(null);

        System.out.println("VEHICULO ENCONTRADO: " + vehiculo);

        return vehiculo;
    }

    public Vehiculo save(Vehiculo vehiculo) {

        if (vehiculoDAO.existsByPlaca(vehiculo.getPlaca())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe un vehículo con esa placa"
            );
        }

        try {

            Usuario usuario = vehiculo.getUsuario();

            if (usuario == null || usuario.getId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El vehículo debe estar asociado a un usuario válido"
                );
            }

            Usuario usuarioBD = usuarioDAO.findById(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Usuario no encontrado"
                    ));

            // 🔥 ASOCIAR VEHÍCULO AL USUARIO
            vehiculo.setUsuario(usuarioBD);

            // 🔥 GUARDAR VEHÍCULO
            return vehiculoDAO.save(vehiculo);

        } catch (DataIntegrityViolationException e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Error de integridad de datos (posible duplicado)"
            );
        }
    }

    public void delete(Long id) {
        vehiculoDAO.deleteById(id);
    }
}