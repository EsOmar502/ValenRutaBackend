package progresa.proyectovalenruta.Service;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DAO.MensajeDAO;
import progresa.proyectovalenruta.DAO.UsuarioDAO;
import progresa.proyectovalenruta.DTO.ConversacionDTO;
import progresa.proyectovalenruta.DTO.MensajeResponseDTO;
import progresa.proyectovalenruta.Entity.Mensaje;
import progresa.proyectovalenruta.Entity.Usuario;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MensajeService {

    private final MensajeDAO mensajeDAO;
    private final UsuarioDAO usuarioDAO;

    public MensajeService(MensajeDAO mensajeDAO, UsuarioDAO usuarioDAO) {
        this.mensajeDAO = mensajeDAO;
        this.usuarioDAO = usuarioDAO;
    }

    // ── Get all messages for a user (legacy) ─────
    public List<Mensaje> getMensajesUsuario(String email) {
        return mensajeDAO
                .findByEmisor_EmailOrReceptor_EmailOrderByFechaAsc(email, email);
    }

    // ── Send message ─────────────────────────────
    @Transactional
    public MensajeResponseDTO enviarMensaje(String email, Long receptorId, String contenido) {

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

        if (contenido == null || contenido.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El mensaje no puede estar vacío"
            );
        }

        Mensaje mensaje = new Mensaje();
        mensaje.setEmisor(emisor);
        mensaje.setReceptor(receptor);
        mensaje.setContenido(contenido.trim());
        mensaje.setFecha(LocalDateTime.now());
        mensaje.setLeido(false);

        Mensaje saved = mensajeDAO.save(mensaje);

        return toResponseDTO(saved);
    }

    // ── Get conversations list ───────────────────
    public List<ConversacionDTO> getConversaciones(Long userId) {

        List<Mensaje> allMessages = mensajeDAO.findByEmisor_EmailOrReceptor_EmailOrderByFechaAsc(
                getUserEmail(userId), getUserEmail(userId)
        );

        // Group by the OTHER user's ID
        Map<Long, List<Mensaje>> grouped = new LinkedHashMap<>();

        for (Mensaje m : allMessages) {
            Long otherUserId = m.getEmisor().getId().equals(userId)
                    ? m.getReceptor().getId()
                    : m.getEmisor().getId();

            grouped.computeIfAbsent(otherUserId, k -> new ArrayList<>()).add(m);
        }

        List<ConversacionDTO> result = new ArrayList<>();

        for (Map.Entry<Long, List<Mensaje>> entry : grouped.entrySet()) {

            Long otherUserId = entry.getKey();
            List<Mensaje> msgs = entry.getValue();
            Mensaje lastMsg = msgs.get(msgs.size() - 1);

            // Get other user info
            Usuario otherUser = lastMsg.getEmisor().getId().equals(otherUserId)
                    ? lastMsg.getEmisor()
                    : lastMsg.getReceptor();

            int noLeidos = mensajeDAO.countNoLeidos(otherUserId, userId);

            ConversacionDTO dto = new ConversacionDTO();
            dto.setUsuarioId(otherUserId);
            dto.setUsuarioNombre(otherUser.getNombre());
            dto.setIniciales(getInitials(otherUser.getNombre()));
            dto.setUltimoMensaje(truncate(lastMsg.getContenido(), 60));
            dto.setFechaUltimoMensaje(lastMsg.getFecha() != null ? lastMsg.getFecha().toString() : null);
            dto.setNoLeidos(noLeidos);

            result.add(dto);
        }

        // Sort by last message date descending (most recent first)
        result.sort((a, b) -> {
            if (a.getFechaUltimoMensaje() == null) return 1;
            if (b.getFechaUltimoMensaje() == null) return -1;
            return b.getFechaUltimoMensaje().compareTo(a.getFechaUltimoMensaje());
        });

        return result;
    }

    // ── Get conversation with specific user ──────
    @Transactional
    public List<MensajeResponseDTO> getConversacion(Long userId, Long otherUserId) {

        // Mark incoming messages as read
        mensajeDAO.marcarComoLeidos(otherUserId, userId);

        List<Mensaje> msgs = mensajeDAO.findConversacion(userId, otherUserId);

        return msgs.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── Count total unread ───────────────────────
    public int countNoLeidos(Long userId) {
        return mensajeDAO.countByReceptor_IdAndLeidoFalse(userId);
    }

    // ── Helpers ──────────────────────────────────
    private MensajeResponseDTO toResponseDTO(Mensaje m) {
        MensajeResponseDTO dto = new MensajeResponseDTO();
        dto.setId(m.getId());
        dto.setContenido(m.getContenido());
        dto.setFecha(m.getFecha() != null ? m.getFecha().toString() : null);
        dto.setLeido(m.isLeido());
        dto.setEmisorId(m.getEmisor().getId());
        dto.setEmisorNombre(m.getEmisor().getNombre());
        dto.setReceptorId(m.getReceptor().getId());
        dto.setReceptorNombre(m.getReceptor().getNombre());
        return dto;
    }

    private String getUserEmail(Long userId) {
        return usuarioDAO.findById(userId)
                .map(Usuario::getEmail)
                .orElse("");
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "…" : text;
    }
}