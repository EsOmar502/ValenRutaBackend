package progresa.proyectovalenruta.Service;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DAO.ReservaDAO;
import progresa.proyectovalenruta.DTO.ReservaConductorDTO;
import progresa.proyectovalenruta.DTO.ReservaDTO;
import progresa.proyectovalenruta.DTO.ReservaResponseDTO;
import progresa.proyectovalenruta.Entity.EstadoReserva;
import progresa.proyectovalenruta.Entity.EstadoViaje;
import progresa.proyectovalenruta.Entity.Reserva;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Viaje;

import java.util.List;

@Service
public class ReservaService {

    private final ReservaDAO reservaDAO;
    private final ViajeService viajeService;

    public ReservaService(ReservaDAO reservaDAO, ViajeService viajeService) {
        this.reservaDAO = reservaDAO;
        this.viajeService = viajeService;
    }

    public List<Reserva> getAll() {
        return reservaDAO.findAll();
    }

    public List<Reserva> getByUsuarioId(Long usuarioId) {
        return reservaDAO.findByUsuario_Id(usuarioId);
    }

    public Reserva getById(Long id) {
        return reservaDAO.findById(id).orElse(null);
    }

    public List<ReservaResponseDTO> getReservasDTO(Long usuarioId) {

        return reservaDAO.findByUsuario_Id(usuarioId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ============================
    // 🚀 CREAR RESERVA
    // ============================
    @Transactional
    public Reserva crearReserva(Usuario usuario, Viaje viaje, int asientosReservados) {

        if (usuario == null || usuario.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario inválido");
        }

        if (viaje == null || viaje.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Viaje inválido");
        }

        Viaje viajeBD = viajeService.getById(viaje.getId());

        if (viajeBD == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Viaje inválido");
        }

        if (asientosReservados <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe reservar al menos 1 asiento"
            );
        }

        if (viajeBD.getEstado() == EstadoViaje.FINALIZADO
                || viajeBD.getEstado() == EstadoViaje.CANCELADO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede reservar un viaje finalizado o cancelado"
            );
        }

        if (viajeBD.getConductor() == null
                || viajeBD.getConductor().getUsuario() == null
                || viajeBD.getConductor().getUsuario().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Viaje sin conductor válido");
        }

        if (viajeBD.getConductor().getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes reservar tu propio viaje"
            );
        }

        if (reservaDAO.existsByUsuario_IdAndViaje_Id(
                usuario.getId(),
                viajeBD.getId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya tienes una reserva para este viaje"
            );
        }

        Integer asientosDisponibles = viajeBD.getAsientosDisponibles();

        if (asientosDisponibles == null || asientosDisponibles < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El viaje tiene plazas inválidas"
            );
        }

        if (asientosDisponibles <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Este viaje está completo"
            );
        }

        if (asientosDisponibles < asientosReservados) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No hay suficientes asientos disponibles"
            );
        }

        int plazasRestantes = asientosDisponibles - asientosReservados;

        if (plazasRestantes < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No hay suficientes asientos disponibles"
            );
        }

        viajeBD.setAsientosDisponibles(plazasRestantes);

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setViaje(viajeBD);
        reserva.setAsientosReservados(asientosReservados);

        reserva.setEstado(EstadoReserva.PENDIENTE);

        return reservaDAO.save(reserva);
    }

    // ============================
    // ✅ ACEPTAR RESERVA
    // ============================
    @Transactional
    public Reserva aceptarReserva(Long id, Long usuarioAutenticadoId) {

        Reserva reserva = reservaDAO.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reserva no encontrada"
                ));

        validarConductorDeReserva(reserva, usuarioAutenticadoId);

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se pueden aceptar reservas en estado PENDIENTE"
            );
        }

        reserva.setEstado(EstadoReserva.ACEPTADA);
        return reservaDAO.save(reserva);
    }

    // ============================
    // ▶️ INICIAR RESERVA
    // ============================
    @Transactional
    public Reserva iniciarReserva(Long id, Long usuarioAutenticadoId) {

        Reserva reserva = reservaDAO.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reserva no encontrada"
                ));

        validarConductorDeReserva(reserva, usuarioAutenticadoId);

        if (reserva.getEstado() != EstadoReserva.ACEPTADA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se pueden iniciar reservas en estado ACEPTADA"
            );
        }

        reserva.setEstado(EstadoReserva.EN_CURSO);
        return reservaDAO.save(reserva);
    }

    // ============================
    // 🏁 FINALIZAR RESERVA
    // ============================
    @Transactional
    public Reserva finalizarReserva(Long id, Long usuarioAutenticadoId) {

        Reserva reserva = reservaDAO.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reserva no encontrada"
                ));

        validarConductorDeReserva(reserva, usuarioAutenticadoId);

        if (reserva.getEstado() != EstadoReserva.EN_CURSO
                && reserva.getEstado() != EstadoReserva.ACEPTADA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se pueden finalizar reservas en estado ACEPTADA o EN_CURSO"
            );
        }

        reserva.setEstado(EstadoReserva.FINALIZADA);
        return reservaDAO.save(reserva);
    }

    // ============================
    // ❌ CANCELAR RESERVA
    // ============================
    @Transactional
    public Reserva cancelarReserva(Long id, Long usuarioAutenticadoId) {

        Reserva reserva = reservaDAO.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reserva no encontrada"
                ));

        validarPropietarioDeReserva(reserva, usuarioAutenticadoId);

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La reserva ya está cancelada"
            );
        }

        if (reserva.getEstado() == EstadoReserva.FINALIZADA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede cancelar una reserva finalizada"
            );
        }

        // 🔥 DEVOLVER ASIENTOS
        Viaje viaje = reserva.getViaje();
        viaje.setAsientosDisponibles(
                viaje.getAsientosDisponibles() + reserva.getAsientosReservados()
        );

        // 🔥 CAMBIAR ESTADO
        reserva.setEstado(EstadoReserva.CANCELADA);

        return reservaDAO.save(reserva);
    }

    private void validarConductorDeReserva(Reserva reserva, Long usuarioAutenticadoId) {
        if (usuarioAutenticadoId == null
                || reserva.getViaje() == null
                || reserva.getViaje().getConductor() == null
                || reserva.getViaje().getConductor().getUsuario() == null
                || !usuarioAutenticadoId.equals(reserva.getViaje().getConductor().getUsuario().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
    }

    private void validarPropietarioDeReserva(Reserva reserva, Long usuarioAutenticadoId) {
        if (usuarioAutenticadoId == null
                || reserva.getUsuario() == null
                || !usuarioAutenticadoId.equals(reserva.getUsuario().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
    }

    // ============================
    // 🔄 ACTUALIZAR RESERVA
    // ============================
    @Transactional
    public Reserva actualizarReserva(Reserva existente, ReservaDTO dto) {

        Viaje nuevoViaje = viajeService.getById(dto.getViajeId());

        if (nuevoViaje == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Viaje no encontrado"
            );
        }

        int nuevosAsientos = dto.getAsientosReservados();

        if (nuevosAsientos <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cantidad inválida"
            );
        }

        // 🚫 No reservar su propio viaje
        if (nuevoViaje.getConductor().getUsuario().getId()
                .equals(existente.getUsuario().getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes reservar tu propio viaje"
            );
        }

        // 🔄 Devolver asientos al anterior
        Viaje viajeAnterior = existente.getViaje();
        viajeAnterior.setAsientosDisponibles(
                viajeAnterior.getAsientosDisponibles() + existente.getAsientosReservados()
        );

        // 🚫 Validar nuevo viaje
        if (nuevoViaje.getAsientosDisponibles() < nuevosAsientos) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No hay suficientes asientos en el nuevo viaje"
            );
        }

        // 🔥 Restar en nuevo viaje
        nuevoViaje.setAsientosDisponibles(
                nuevoViaje.getAsientosDisponibles() - nuevosAsientos
        );

        existente.setViaje(nuevoViaje);
        existente.setAsientosReservados(nuevosAsientos);

        return reservaDAO.save(existente);
    }

    // ============================
    // 🧨 ELIMINAR
    // ============================
    @Transactional
    public void delete(Long id) {

        Reserva reserva = reservaDAO.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Reserva no encontrada"
                        )
                );

        Viaje viaje = reserva.getViaje();

        // 🔁 DEVOLVER ASIENTOS
        viaje.setAsientosDisponibles(
                viaje.getAsientosDisponibles() + reserva.getAsientosReservados()
        );

        reservaDAO.delete(reserva);
    }

    // ============================
    // 📋 RESERVAS ACTIVAS (PENDIENTE, ACEPTADA, EN_CURSO)
    // ============================
    public List<Reserva> getActivasByUsuario(Long usuarioId) {
        return reservaDAO.findByUsuario_IdAndEstadoIn(
                usuarioId,
                List.of(EstadoReserva.PENDIENTE, EstadoReserva.ACEPTADA, EstadoReserva.EN_CURSO)
        );
    }

    // ============================
    // 🏁 VIAJES REALIZADOS (FINALIZADA)
    // ============================
    public List<Reserva> getFinalizadasByUsuario(Long usuarioId) {
        return reservaDAO.findByUsuario_IdAndEstado(usuarioId, EstadoReserva.FINALIZADA);
    }

    // ============================
    // 🚗 RESERVAS DE MIS VIAJES (para conductor)
    // ============================
    public List<ReservaConductorDTO> getReservasDeMisViajes(Long usuarioId) {

        return reservaDAO.findAll().stream()

                // 🔥 FILTRO: reservas no canceladas
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)

                // 🔥 FILTRO: viajes donde soy conductor
                .filter(r -> r.getViaje()
                        .getConductor()
                        .getUsuario()
                        .getId()
                        .equals(usuarioId))

                .map(r -> {
                    ReservaConductorDTO dto = new ReservaConductorDTO();

                    dto.setReservaId(r.getId());
                    dto.setViajeId(r.getViaje().getId());
                    dto.setUsuarioId(r.getUsuario().getId());
                    dto.setUsuarioNombre(r.getUsuario().getNombre());
                    dto.setUsuarioEmail(r.getUsuario().getEmail());
                    dto.setUsuarioRating(ratingUsuario(r.getUsuario()));
                    dto.setAsientos(r.getAsientosReservados());

                    dto.setOrigen(r.getViaje().getOrigen());
                    dto.setDestino(r.getViaje().getDestino());
                    dto.setFechaSalida(r.getViaje().getFechaSalida() != null ? r.getViaje().getFechaSalida().toString() : null);

                    EstadoViaje estadoViaje = r.getViaje().getEstado();
                    dto.setEstadoViaje(estadoViaje != null ? estadoViaje.name() : null);

                    // Mapeo manual solicitado: si el viaje está en curso o finalizado, la reserva refleja ese estado
                    if (estadoViaje == EstadoViaje.EN_CURSO) {
                        dto.setEstadoReserva(EstadoReserva.EN_CURSO.name());
                    } else if (estadoViaje == EstadoViaje.FINALIZADO) {
                        dto.setEstadoReserva(EstadoReserva.FINALIZADA.name());
                    } else {
                        dto.setEstadoReserva(r.getEstado() != null ? r.getEstado().name() : null);
                    }

                    return dto;
                })
                .toList();
    }

    // ============================
    // 🔧 HELPER: Mapear Reserva → ReservaResponseDTO
    // ============================
    public ReservaResponseDTO toResponseDTO(Reserva r) {
        ReservaResponseDTO dto = new ReservaResponseDTO();

        dto.setId(r.getId());
        dto.setAsientosReservados(r.getAsientosReservados());

        dto.setUsuarioNombre(r.getUsuario().getNombre());
        dto.setUsuarioEmail(r.getUsuario().getEmail());
        dto.setUsuarioRating(ratingUsuario(r.getUsuario()));

        if (r.getViaje().getConductor() != null && r.getViaje().getConductor().getUsuario() != null) {
            dto.setConductorNombre(r.getViaje().getConductor().getUsuario().getNombre());
            dto.setConductorId(r.getViaje().getConductor().getUsuario().getId());
            dto.setConductorRating(ratingUsuario(r.getViaje().getConductor().getUsuario()));
        }

        dto.setOrigen(r.getViaje().getOrigen());
        dto.setDestino(r.getViaje().getDestino());
        dto.setFechaSalida(r.getViaje().getFechaSalida() != null ? r.getViaje().getFechaSalida().toString() : null);
        dto.setPrecio(r.getViaje().getPrecio());

        // Nuevos campos
        dto.setEstado(r.getEstado() != null ? r.getEstado().name() : null);
        dto.setAsientosDisponibles(r.getViaje().getAsientosDisponibles());
        dto.setViajeId(r.getViaje().getId());

        return dto;
    }

    private Double ratingUsuario(Usuario usuario) {
        return usuario != null && usuario.getRating() != null ? usuario.getRating() : 0.0;
    }
}
