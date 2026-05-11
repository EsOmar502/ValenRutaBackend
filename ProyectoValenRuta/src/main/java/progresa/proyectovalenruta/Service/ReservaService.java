package progresa.proyectovalenruta.Service;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DAO.ReservaDAO;
import progresa.proyectovalenruta.DTO.ReservaConductorDTO;
import progresa.proyectovalenruta.DTO.ReservaDTO;
import progresa.proyectovalenruta.DTO.ReservaResponseDTO;
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
                .map(r -> {
                    ReservaResponseDTO dto = new ReservaResponseDTO();

                    dto.setId(r.getId());
                    dto.setAsientosReservados(r.getAsientosReservados());

                    dto.setUsuarioNombre(r.getUsuario().getNombre());
                    dto.setUsuarioEmail(r.getUsuario().getEmail());

                    dto.setOrigen(r.getViaje().getOrigen());
                    dto.setDestino(r.getViaje().getDestino());
                    dto.setFechaSalida(r.getViaje().getFechaSalida() != null ? r.getViaje().getFechaSalida().toString() : null);
                    dto.setPrecio(r.getViaje().getPrecio());

                    return dto;
                })
                .toList();
    }

    // ============================
    // 🚀 CREAR RESERVA (CORREGIDO)
    // ============================
    @Transactional
    public Reserva crearReserva(Usuario usuario, Viaje viaje, int asientosReservados) {

        if (usuario == null || usuario.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario inválido");
        }

        if (viaje == null || viaje.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Viaje inválido");
        }

        if (asientosReservados <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe reservar al menos 1 asiento"
            );
        }

        // 🚫 No reservar su propio viaje
        if (viaje.getConductor().getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes reservar tu propio viaje"
            );
        }

        // 🚫 Duplicado SOLO si no está cancelada
        if (reservaDAO.existsByUsuario_IdAndViaje_IdAndEstadoNot(
                usuario.getId(),
                viaje.getId(),
                "CANCELADA"
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya tienes una reserva activa para este viaje"
            );
        }

        // 🚫 Asientos
        if (viaje.getAsientosDisponibles() < asientosReservados) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No hay suficientes asientos disponibles"
            );
        }

        // 🔥 RESTAR ASIENTOS
        viaje.setAsientosDisponibles(
                viaje.getAsientosDisponibles() - asientosReservados
        );

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setViaje(viaje);
        reserva.setAsientosReservados(asientosReservados);

        // 🔥 ESTADO POR DEFECTO
        reserva.setEstado("CONFIRMADA");

        return reservaDAO.save(reserva);
    }

    // Cancelar Reserva

    @Transactional
    public Reserva cancelarReserva(Long id) {

        Reserva reserva = reservaDAO.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reserva no encontrada"
                ));

        if ("CANCELADA".equals(reserva.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La reserva ya está cancelada"
            );
        }

        // 🔥 DEVOLVER ASIENTOS
        Viaje viaje = reserva.getViaje();
        viaje.setAsientosDisponibles(
                viaje.getAsientosDisponibles() + reserva.getAsientosReservados()
        );

        // 🔥 CAMBIAR ESTADO
        reserva.setEstado("CANCELADA");

        return reservaDAO.save(reserva);
    }

    // ============================
    // 🔄 ACTUALIZAR RESERVA (MEJORADO)
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
    // 🧨 ELIMINAR (CORRECTO)
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

    public List<Reserva> getActivasByUsuario(Long usuarioId) {
        return reservaDAO.findByUsuario_IdAndEstado(usuarioId, "CONFIRMADA");
    }

    public List<ReservaConductorDTO> getReservasDeMisViajes(Long usuarioId) {

        return reservaDAO.findAll().stream()

                // 🔍 DEBUG GLOBAL (ANTES DE TODO)
                .peek(r -> {
                    System.out.println("-----");
                    System.out.println("Reserva ID: " + r.getId());
                    System.out.println("Estado: " + r.getEstado());
                    System.out.println("Usuario reserva: " + r.getUsuario().getId());
                    System.out.println("Conductor usuario: " + r.getViaje().getConductor().getUsuario().getId());
                    System.out.println("Usuario logueado: " + usuarioId);
                })

                // 🔥 FILTRO 1
                .filter(r -> "CONFIRMADA".equals(r.getEstado()))

                // 🔥 FILTRO 2
                .filter(r -> r.getViaje()
                        .getConductor()
                        .getUsuario()
                        .getId()
                        .equals(usuarioId))

                .map(r -> {
                    ReservaConductorDTO dto = new ReservaConductorDTO();

                    dto.setReservaId(r.getId());
                    dto.setUsuarioNombre(r.getUsuario().getNombre());
                    dto.setUsuarioEmail(r.getUsuario().getEmail());
                    dto.setAsientos(r.getAsientosReservados());

                    dto.setOrigen(r.getViaje().getOrigen());
                    dto.setDestino(r.getViaje().getDestino());
                    dto.setFechaSalida(r.getViaje().getFechaSalida() != null ? r.getViaje().getFechaSalida().toString() : null);

                    dto.setEstadoViaje(r.getViaje().getEstado());


                    return dto;
                })
                .toList();
    }

}