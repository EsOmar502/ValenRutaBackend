package progresa.proyectovalenruta.Service;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DAO.ReservaDAO;
import progresa.proyectovalenruta.DAO.UsuarioDAO;
import progresa.proyectovalenruta.DAO.ValoracionDAO;
import progresa.proyectovalenruta.DAO.ViajeDAO;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Valoracion;
import progresa.proyectovalenruta.Entity.Viaje;

import java.util.List;

@Service
public class ValoracionService {

    private final ValoracionDAO valoracionDAO;
    private final UsuarioDAO usuarioDAO;
    private final ViajeDAO viajeDAO;
    private final ReservaDAO reservaDAO;

    public ValoracionService(
            ValoracionDAO valoracionDAO,
            UsuarioDAO usuarioDAO,
            ViajeDAO viajeDAO,
            ReservaDAO reservaDAO
    ) {
        this.valoracionDAO = valoracionDAO;
        this.usuarioDAO = usuarioDAO;
        this.viajeDAO = viajeDAO;
        this.reservaDAO = reservaDAO;
    }


    public List<Valoracion> getAll() {
        return valoracionDAO.findAll();
    }

    public Valoracion getById(Long id) {
        return valoracionDAO.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Valoración no encontrada"
                        )
                );
    }


    @Transactional
    public Valoracion crearValoracionDesdeToken(
            String email,
            Long evaluadoId,
            Long viajeId,
            int puntuacion,
            String comentario
    ) {

        Usuario evaluador = usuarioDAO.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Evaluador no encontrado"
                        )
                );

        Usuario evaluado = usuarioDAO.findById(evaluadoId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Evaluado no encontrado"
                        )
                );

        Viaje viaje = viajeDAO.findById(viajeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Viaje no encontrado"
                        )
                );

        if (evaluador.getId().equals(evaluado.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No puedes valorarte a ti mismo"
            );
        }

        boolean esConductor = viaje.getConductor() != null &&
                viaje.getConductor().getUsuario() != null &&
                viaje.getConductor().getUsuario().getId().equals(evaluador.getId());

        boolean esPasajero = reservaDAO.existsByUsuarioAndViaje(evaluador, viaje);

        if (!esConductor && !esPasajero) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No participaste en este viaje"
            );
        }

        boolean yaExiste = valoracionDAO
                .existsByEvaluadorAndEvaluadoAndViaje(evaluador, evaluado, viaje);

        if (yaExiste) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya has valorado a este usuario en este viaje"
            );
        }

        Valoracion valoracion = new Valoracion();
        valoracion.setEvaluador(evaluador);
        valoracion.setEvaluado(evaluado);
        valoracion.setViaje(viaje);
        valoracion.setPuntuacion(puntuacion);
        valoracion.setComentario(comentario);

        return valoracionDAO.save(valoracion);
    }



    @Transactional
    public Valoracion save(Valoracion valoracion) {
        return valoracionDAO.save(valoracion);
    }



    public void delete(Long id) {
        Valoracion valoracion = getById(id);
        valoracionDAO.delete(valoracion);
    }

    @Transactional
    public Valoracion eliminarComentario(Long id, String email) {

        Valoracion valoracion = valoracionDAO.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Valoración no encontrada"
                        )
                );

        if (!valoracion.getEvaluador()
                .getEmail()
                .equalsIgnoreCase(email)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No autorizado"
            );
        }

        valoracion.setComentario(null);

        return valoracionDAO.save(valoracion);
    }
}