package progresa.proyectovalenruta.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DAO.ConductorDAO;
import progresa.proyectovalenruta.Entity.Conductor;

import java.util.List;

@Service
public class ConductorService {

    @Autowired
    private ConductorDAO conductorDAO;

    public List<Conductor> getAll() {
        return conductorDAO.findAll();
    }

    public Conductor getById(Long id) {
        return conductorDAO.findById(id).orElse(null);
    }

    public Conductor save(Conductor conductor) {
        return conductorDAO.save(conductor);
    }

    public void delete(Long id) {
        conductorDAO.deleteById(id);
    }

    public Conductor getByUsuarioId(Long usuarioId) {
        return conductorDAO.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El usuario no es conductor"
                ));
    }

}
