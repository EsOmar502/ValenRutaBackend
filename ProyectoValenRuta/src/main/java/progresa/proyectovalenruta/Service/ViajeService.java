package progresa.proyectovalenruta.Service;

import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DAO.ConductorDAO;
import progresa.proyectovalenruta.DAO.ReservaDAO;
import progresa.proyectovalenruta.DAO.UsuarioDAO;
import progresa.proyectovalenruta.DAO.ViajeDAO;
import progresa.proyectovalenruta.DTO.ViajeCercanoDTO;
import progresa.proyectovalenruta.DTO.ViajeDetalleDTO;
import progresa.proyectovalenruta.DTO.ViajeDisponibleDTO;
import progresa.proyectovalenruta.Entity.Conductor;
import progresa.proyectovalenruta.Entity.EstadoReserva;
import progresa.proyectovalenruta.Entity.EstadoViaje;
import progresa.proyectovalenruta.Entity.Reserva;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Viaje;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ViajeService {

    private final ViajeDAO viajeDAO;
    private final ConductorDAO conductorDAO;
    private final ReservaDAO reservaDAO;
    private final MensajeService mensajeService;
    private final UsuarioDAO usuarioDAO;

    public ViajeService(ViajeDAO viajeDAO, ConductorDAO conductorDAO, ReservaDAO reservaDAO, MensajeService mensajeService, UsuarioDAO usuarioDAO) {
        this.viajeDAO = viajeDAO;
        this.conductorDAO = conductorDAO;
        this.reservaDAO = reservaDAO;
        this.mensajeService = mensajeService;
        this.usuarioDAO = usuarioDAO;
    }

    public List<Viaje> getAll() {
        return viajeDAO.findAll();
    }

    public Viaje getById(Long id) {
        return viajeDAO.findById(id).orElse(null);
    }

    public List<Viaje> buscarDisponibles(String origen, String destino, LocalDateTime fechaSalida) {
        return viajeDAO.buscarDisponibles(origen, destino, fechaSalida);
    }

    public Viaje save(Viaje viaje) {
        try {
            if (viaje.getConductor() == null || viaje.getConductor().getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe especificar un conductor válido");
            }

            Conductor conductor = conductorDAO.findById(viaje.getConductor().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conductor no encontrado"));

            String origen = viaje.getOrigen() != null ? viaje.getOrigen().trim() : null;
            String destino = viaje.getDestino() != null ? viaje.getDestino().trim() : null;

            viaje.setOrigen(origen);
            viaje.setDestino(destino);

            boolean existe = viajeDAO.existsByOrigenAndDestinoAndFechaSalidaAndConductor_Id(
                    origen, destino, viaje.getFechaSalida(), conductor.getId()
            );

            if (existe) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un viaje con la misma información para este conductor.");
            }

            viaje.setConductor(conductor);
            viaje.setEstado(EstadoViaje.PROGRAMADO);

            return viajeDAO.save(viaje);

        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Intento de duplicado detectado.");
        }
    }

    public Viaje update(Viaje viaje) {
        return viajeDAO.save(viaje);
    }

    public void delete(Long id) {
        viajeDAO.deleteById(id);
    }

    public List<ViajeCercanoDTO> buscarCercanos(Double lat, Double lng, Double radioKm) {
        return viajeDAO.findAll().stream()
                .filter(v -> v.getLatOrigen() != null && v.getLngOrigen() != null)
                .filter(v -> v.getEstado() != EstadoViaje.FINALIZADO)
                .filter(v -> v.getEstado() != EstadoViaje.CANCELADO)
                .map(v -> {
                    double distancia = calcularDistancia(lat, lng, v.getLatOrigen(), v.getLngOrigen());
                    if (distancia <= radioKm) {
                        ViajeCercanoDTO dto = new ViajeCercanoDTO();
                        dto.setId(v.getId());
                        dto.setOrigen(v.getOrigen());
                        dto.setDestino(v.getDestino());
                        dto.setPrecio(v.getPrecio());
                        dto.setDistanciaKm(Math.round(distancia * 100.0) / 100.0);
                        dto.setLatOrigen(v.getLatOrigen());
                        dto.setLngOrigen(v.getLngOrigen());
                        dto.setLatDestino(v.getLatDestino());
                        dto.setLngDestino(v.getLngDestino());
                        dto.setFechaSalida(v.getFechaSalida() != null ? v.getFechaSalida().toString() : null);
                        return dto;
                    }
                    return null;
                })
                .filter(v -> v != null)
                .sorted((a, b) -> Double.compare(a.getDistanciaKm(), b.getDistanciaKm()))
                .toList();
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Transactional
    public Viaje iniciarViaje(Long viajeId, Long conductorUserId) {
        Viaje viaje = viajeDAO.findById(viajeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));

        if (!viaje.getConductor().getUsuario().getId().equals(conductorUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        if (viaje.getEstado() == EstadoViaje.FINALIZADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El viaje ya está finalizado");
        }

        if (viaje.getEstado() == EstadoViaje.EN_CURSO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El viaje ya está en curso");
        }

        viaje.setEstado(EstadoViaje.EN_CURSO);

        List<Reserva> reservas = reservaDAO.findByViaje_Id(viajeId);
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() != EstadoReserva.CANCELADA && reserva.getEstado() != EstadoReserva.FINALIZADA) {
                reserva.setEstado(EstadoReserva.EN_CURSO);
                reservaDAO.save(reserva);
                
                // Enviar notificación a los pasajeros
                String emisorEmail = viaje.getConductor().getUsuario().getEmail();
                Long receptorId = reserva.getUsuario().getId();
                try {
                    mensajeService.enviarMensaje(emisorEmail, receptorId, "El conductor ha iniciado el viaje");
                } catch (Exception e) {
                    // Ignore message errors to not block the main flow
                }
            }
        }

        return viajeDAO.save(viaje);
    }

    @Transactional
    public Viaje finalizarViaje(Long viajeId, Long conductorUserId) {
        Viaje viaje = viajeDAO.findById(viajeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));

        if (!viaje.getConductor().getUsuario().getId().equals(conductorUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        if (viaje.getEstado() != EstadoViaje.EN_CURSO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El viaje debe estar en curso para poder finalizarse");
        }

        viaje.setEstado(EstadoViaje.FINALIZADO);

        List<Reserva> reservasDelViaje = reservaDAO.findByViaje_Id(viajeId);
        for (Reserva reserva : reservasDelViaje) {
            if (reserva.getEstado() == EstadoReserva.EN_CURSO || reserva.getEstado() == EstadoReserva.ACEPTADA) {
                reserva.setEstado(EstadoReserva.FINALIZADA);
                reservaDAO.save(reserva);
                
                // Enviar notificación a los pasajeros
                String emisorEmail = viaje.getConductor().getUsuario().getEmail();
                Long receptorId = reserva.getUsuario().getId();
                try {
                    mensajeService.enviarMensaje(emisorEmail, receptorId, "El viaje ha finalizado. Ya puedes calificar al conductor");
                } catch (Exception e) {
                    // Ignore message errors
                }
            }
        }

        return viajeDAO.save(viaje);
    }

    private Usuario getCurrentUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return usuarioDAO.findByEmail(auth.getName()).orElse(null);
    }

    private ViajeDisponibleDTO toViajeDisponibleDTO(Viaje viaje, Usuario currentUsuario) {
        boolean puedeIniciar = false;
        boolean puedeFinalizar = false;

        if (currentUsuario != null && viaje.getConductor().getUsuario().getId().equals(currentUsuario.getId())) {
            if (viaje.getEstado() == EstadoViaje.PROGRAMADO) {
                puedeIniciar = true;
            } else if (viaje.getEstado() == EstadoViaje.EN_CURSO) {
                puedeFinalizar = true;
            }
        }

        return new ViajeDisponibleDTO(
                viaje.getId(),
                viaje.getOrigen(),
                viaje.getDestino(),
                viaje.getFechaSalida() != null ? viaje.getFechaSalida().toString() : null,
                viaje.getPrecio(),
                viaje.getAsientosDisponibles(),
                viaje.getConductor().getUsuario().getNombre(),
                viaje.getConductor().getUsuario().getId(),
                viaje.getLatOrigen(),
                viaje.getLngOrigen(),
                viaje.getLatDestino(),
                viaje.getLngDestino(),
                viaje.getEstado() != null ? viaje.getEstado().name() : null,
                puedeIniciar,
                puedeFinalizar
        );
    }

    public List<ViajeDisponibleDTO> getViajesDisponibles() {
        Usuario currentUsuario = getCurrentUsuario();

        return viajeDAO.findAll()
                .stream()
                .filter(viaje -> viaje.getEstado() != EstadoViaje.FINALIZADO)
                .filter(viaje -> viaje.getEstado() != EstadoViaje.CANCELADO)
                .map(viaje -> toViajeDisponibleDTO(viaje, currentUsuario))
                .toList();
    }

    public ViajeDetalleDTO getDetalleViaje(Long id) {
        Viaje viaje = viajeDAO.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));

        return new ViajeDetalleDTO(
                viaje.getId(),
                viaje.getOrigen(),
                viaje.getDestino(),
                viaje.getFechaSalida() != null ? viaje.getFechaSalida().toString() : null,
                viaje.getPrecio(),
                viaje.getAsientosDisponibles(),
                viaje.getEstado() != null ? viaje.getEstado().name() : null,
                viaje.getConductor().getUsuario().getNombre(),
                viaje.getLatOrigen(),
                viaje.getLngOrigen(),
                viaje.getLatDestino(),
                viaje.getLngDestino()
        );
    }

    public List<ViajeDisponibleDTO> getMisViajes(Long usuarioId) {
        Usuario currentUsuario = usuarioDAO.findById(usuarioId).orElse(null);

        return viajeDAO.findByConductor_Usuario_Id(usuarioId)
                .stream()
                .map(viaje -> toViajeDisponibleDTO(viaje, currentUsuario))
                .toList();
    }
}