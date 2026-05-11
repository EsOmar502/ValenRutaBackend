package progresa.proyectovalenruta.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DAO.UsuarioDAO;
import progresa.proyectovalenruta.DAO.VehiculoDAO;
import progresa.proyectovalenruta.DAO.ConductorDAO;
import progresa.proyectovalenruta.Entity.Conductor;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Vehiculo;

import java.util.List;

@Service
public class VehiculoService {

    @Autowired
    private VehiculoDAO vehiculoDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private ConductorDAO conductorDAO;

    public List<Vehiculo> getAll() {
        return vehiculoDAO.findAll();
    }

    public Vehiculo getById(Long id) {
        return vehiculoDAO.findById(id).orElse(null);
    }

    public List<Vehiculo> getByUsuario(Long usuarioId) {

        System.out.println("BUSCANDO VEHICULOS PARA USUARIO: " + usuarioId);

        List<Vehiculo> vehiculos =
                vehiculoDAO.findAllByUsuario_Id(usuarioId);

        System.out.println("TOTAL VEHICULOS: " + vehiculos.size());

        return vehiculos;
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

            Conductor conductorExistente =
                    conductorDAO.findByUsuarioId(usuarioBD.getId())
                            .orElse(null);

            if (conductorExistente == null) {

                System.out.println("CREANDO CONDUCTOR AUTOMÁTICO");

                Conductor nuevoConductor = new Conductor();

                nuevoConductor.setUsuario(usuarioBD);

                // MVP
                nuevoConductor.setVerificado(true);

                conductorDAO.save(nuevoConductor);
            }

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