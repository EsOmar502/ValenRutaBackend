package progresa.proyectovalenruta.Service;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import progresa.proyectovalenruta.DAO.ConductorDAO;
import progresa.proyectovalenruta.DAO.ReservaDAO;
import progresa.proyectovalenruta.DAO.UsuarioDAO;
import progresa.proyectovalenruta.DAO.ValoracionDAO;
import progresa.proyectovalenruta.DAO.ViajeDAO;
import progresa.proyectovalenruta.DTO.CrearValoracionDTO;
import progresa.proyectovalenruta.DTO.ValoracionResponseDTO;
import progresa.proyectovalenruta.Entity.Conductor;
import progresa.proyectovalenruta.Entity.EstadoReserva;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Valoracion;
import progresa.proyectovalenruta.Entity.Viaje;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValoracionService {

    private final ValoracionDAO valoracionDAO;
    private final UsuarioDAO usuarioDAO;
    private final ViajeDAO viajeDAO;
    private final ReservaDAO reservaDAO;
    private final ConductorDAO conductorDAO;


    public ValoracionService(
            ValoracionDAO valoracionDAO,
            UsuarioDAO usuarioDAO,
            ViajeDAO viajeDAO,
            ReservaDAO reservaDAO,
            ConductorDAO conductorDAO
    ) {
        this.valoracionDAO = valoracionDAO;
        this.usuarioDAO = usuarioDAO;
        this.viajeDAO = viajeDAO;
        this.reservaDAO = reservaDAO;
        this.conductorDAO = conductorDAO;
    }

    @Transactional
    public ValoracionResponseDTO crearValoracion(CrearValoracionDTO dto, Long usuarioId) {
        Usuario usuarioQueValora = usuarioDAO.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Viaje viaje = viajeDAO.findById(dto.getViajeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));

        Usuario usuarioValorado = null;
        boolean esConductor = viaje.getConductor().getUsuario().getId().equals(usuarioId);

        if (dto.getUsuarioValoradoId() != null) {
            usuarioValorado = usuarioDAO.findById(dto.getUsuarioValoradoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario valorado no encontrado"));
        } else {
            // Retrocompatibilidad: Si no se envía usuarioValoradoId, asumimos que el pasajero valora al conductor
            if (esConductor) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El conductor debe especificar a qué pasajero valora");
            }
            usuarioValorado = viaje.getConductor().getUsuario();
        }

        if (usuarioQueValora.getId().equals(usuarioValorado.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes valorarte a ti mismo");
        }

        if (!"FINALIZADO".equals(viaje.getEstado().name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El viaje aún no ha finalizado");
        }

        final Long usuarioValoradoId = usuarioValorado.getId();

        // Validate relationship based on roles
        if (esConductor) {
            // El conductor valora a un pasajero
            boolean esPasajero = reservaDAO.findByViaje_Id(viaje.getId()).stream()
                    .anyMatch(r -> r.getUsuario().getId().equals(usuarioValoradoId) && r.getEstado() == EstadoReserva.FINALIZADA);
            
            if (!esPasajero) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario valorado no es un pasajero finalizado de este viaje");
            }
        } else {
            // El pasajero valora al conductor
            if (!viaje.getConductor().getUsuario().getId().equals(usuarioValorado.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Como pasajero solo puedes valorar al conductor de este viaje");
            }
            // Verificar que el usuarioQueValora fue pasajero
            boolean fuiPasajero = reservaDAO.findByViaje_Id(viaje.getId()).stream()
                    .anyMatch(r -> r.getUsuario().getId().equals(usuarioQueValora.getId()) && r.getEstado() == EstadoReserva.FINALIZADA);
            
            if (!fuiPasajero) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No fuiste pasajero de este viaje o tu reserva no está finalizada");
            }
        }

        boolean yaExiste = valoracionDAO.existsByUsuarioQueValora_IdAndUsuarioValorado_IdAndViaje_Id(usuarioId, usuarioValorado.getId(), viaje.getId());
        if (yaExiste) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya has valorado a este usuario en este viaje");
        }

        Valoracion valoracion = new Valoracion();
        valoracion.setUsuarioQueValora(usuarioQueValora);
        valoracion.setUsuarioValorado(usuarioValorado);
        valoracion.setViaje(viaje);
        valoracion.setPuntuacion(dto.getPuntuacion());
        valoracion.setComentario(dto.getComentario());

        Valoracion saved = valoracionDAO.save(valoracion);

        actualizarRatingUsuario(usuarioValorado);

        return toResponseDTO(saved);
    }

    private void actualizarRatingUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            return;
        }

        Double promedio = valoracionDAO.calcularPromedioPuntuacionRecibida(usuario.getId());
        usuario.setRating(normalizarRating(promedio));
        usuarioDAO.save(usuario);
    }

    private double normalizarRating(Double promedio) {
        double rating = promedio != null ? promedio : 0.0;

        if (rating < 0.0) {
            rating = 0.0;
        }

        if (rating > 5.0) {
            rating = 5.0;
        }

        return Math.round(rating * 10.0) / 10.0;
    }

    public List<ValoracionResponseDTO> getValoracionesPorConductor(Long conductorId) {
        Conductor conductor = conductorDAO.findById(conductorId).orElse(null);
        if (conductor == null) return List.of();
        
        return valoracionDAO.findByUsuarioValorado_Id(conductor.getUsuario().getId()).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ValoracionResponseDTO> getValoracionesPorUsuario(Long usuarioId) {
        return valoracionDAO.findByUsuarioValorado_Id(usuarioId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private ValoracionResponseDTO toResponseDTO(Valoracion v) {
        ValoracionResponseDTO dto = new ValoracionResponseDTO();
        dto.setUsuarioNombre(v.getUsuarioQueValora().getNombre());
        dto.setPuntuacion(v.getPuntuacion());
        dto.setComentario(v.getComentario());
        dto.setFecha(v.getFechaCreacion() != null ? v.getFechaCreacion().toString() : null);
        dto.setUsuarioValoradoId(v.getUsuarioValorado().getId());
        dto.setUsuarioQueValoraId(v.getUsuarioQueValora().getId());
        dto.setUsuarioValoradoRating(ratingUsuario(v.getUsuarioValorado()));
        dto.setUsuarioQueValoraRating(ratingUsuario(v.getUsuarioQueValora()));
        return dto;
    }

    private Double ratingUsuario(Usuario usuario) {
        return usuario != null && usuario.getRating() != null ? usuario.getRating() : 0.0;
    }
}
