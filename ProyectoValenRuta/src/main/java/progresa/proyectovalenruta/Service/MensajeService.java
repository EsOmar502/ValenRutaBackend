package progresa.proyectovalenruta.Service;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DAO.MensajeDAO;
import progresa.proyectovalenruta.DAO.UsuarioDAO;
import progresa.proyectovalenruta.Entity.Mensaje;
import progresa.proyectovalenruta.Entity.Usuario;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MensajeService {

    private final MensajeDAO mensajeDAO;
    private final UsuarioDAO usuarioDAO;

    public MensajeService(MensajeDAO mensajeDAO, UsuarioDAO usuarioDAO) {
        this.mensajeDAO = mensajeDAO;
        this.usuarioDAO = usuarioDAO;
    }

    public List<Mensaje> getMensajesUsuario(String email) {
        return mensajeDAO
                .findByEmisor_EmailOrReceptor_EmailOrderByFechaAsc(email, email);
    }
    @Transactional
    public Mensaje enviarMensaje(String email, Long receptorId, String contenido) {

        Usuario emisor = usuarioDAO.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Usuario receptor = usuarioDAO.findById(receptorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Receptor no encontrado"));

        if (emisor.getId().equals(receptor.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No puedes enviarte mensaje a ti mismo"
            );
        }

        Mensaje mensaje = new Mensaje();
        mensaje.setEmisor(emisor);
        mensaje.setReceptor(receptor);
        mensaje.setContenido(contenido);
        mensaje.setFecha(LocalDateTime.now());

        return mensajeDAO.save(mensaje);
    }
}