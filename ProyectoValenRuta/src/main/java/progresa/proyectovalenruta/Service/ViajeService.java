package progresa.proyectovalenruta.Service;

import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import progresa.proyectovalenruta.DAO.ViajeDAO;
import progresa.proyectovalenruta.DTO.ViajeDetalleDTO;
import progresa.proyectovalenruta.DTO.ViajeDisponibleDTO;
import progresa.proyectovalenruta.Entity.Conductor;
import progresa.proyectovalenruta.Entity.Viaje;
import progresa.proyectovalenruta.DAO.ConductorDAO;
import progresa.proyectovalenruta.DTO.ViajeCercanoDTO;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ViajeService {

    private final ViajeDAO viajeDAO;
    private final ConductorDAO conductorDAO;

    public ViajeService(ViajeDAO viajeDAO, ConductorDAO conductorDAO) {
        this.viajeDAO = viajeDAO;
        this.conductorDAO = conductorDAO;
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

            // Validación conductor
            if (viaje.getConductor() == null || viaje.getConductor().getId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Debe especificar un conductor válido"
                );
            }

            Conductor conductor = conductorDAO.findById(viaje.getConductor().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Conductor no encontrado"
                    ));

            if (!conductor.isVerificado()) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "El conductor no está verificado"
                );
            }

            // Limpieza básica de strings
            String origen = viaje.getOrigen() != null ? viaje.getOrigen().trim() : null;
            String destino = viaje.getDestino() != null ? viaje.getDestino().trim() : null;

            viaje.setOrigen(origen);
            viaje.setDestino(destino);

            // Validar duplicados
            boolean existe = viajeDAO.existsByOrigenAndDestinoAndFechaSalidaAndConductor_Id(
                    origen,
                    destino,
                    viaje.getFechaSalida(),
                    conductor.getId()
            );

            if (existe) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ya existe un viaje con la misma información para este conductor."
                );
            }

            viaje.setConductor(conductor);
            viaje.setEstado("PROGRAMADO");

            return viajeDAO.save(viaje);

        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Intento de duplicado detectado."
            );
        }
    }

    public void delete(Long id) {
        viajeDAO.deleteById(id);
    }

    // Búsqueda por cercanía con Haversine
    public List<ViajeCercanoDTO> buscarCercanos(Double lat, Double lng, Double radioKm) {

        return viajeDAO.findAll().stream()
                .filter(v -> v.getLatOrigen() != null && v.getLngOrigen() != null)
                .map(v -> {

                    double distancia = calcularDistancia(
                            lat,
                            lng,
                            v.getLatOrigen(),
                            v.getLngOrigen()
                    );

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

    // Fórmula Haversine
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
    public Viaje finalizarViaje(Long viajeId) {

        Viaje viaje = viajeDAO.findById(viajeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Viaje no encontrado"
                ));

        if ("FINALIZADO".equals(viaje.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede reservar un viaje finalizado"
            );
        }

        viaje.setEstado("FINALIZADO");

        return viajeDAO.save(viaje);
    }

    public List<ViajeDisponibleDTO> getViajesDisponibles() {

        return viajeDAO.findAll()
                .stream()
                .filter(viaje -> !"FINALIZADO".equals(viaje.getEstado()))
                .filter(viaje -> viaje.getAsientosDisponibles() > 0)
                .map(viaje -> new ViajeDisponibleDTO(
                        viaje.getId(),
                        viaje.getOrigen(),
                        viaje.getDestino(),
                        viaje.getFechaSalida() != null ? viaje.getFechaSalida().toString() : null,
                        viaje.getPrecio(),
                        viaje.getAsientosDisponibles(),
                        viaje.getConductor().getUsuario().getNombre(),
                        viaje.getLatOrigen(),
                        viaje.getLngOrigen(),
                        viaje.getLatDestino(),
                        viaje.getLngDestino()
                ))
                .toList();
    }

    public ViajeDetalleDTO getDetalleViaje(Long id) {

        Viaje viaje = viajeDAO.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Viaje no encontrado"
                ));

        return new ViajeDetalleDTO(
                viaje.getId(),
                viaje.getOrigen(),
                viaje.getDestino(),
                viaje.getFechaSalida() != null ? viaje.getFechaSalida().toString() : null,
                viaje.getPrecio(),
                viaje.getAsientosDisponibles(),
                viaje.getEstado(),
                viaje.getConductor().getUsuario().getNombre(),
                viaje.getLatOrigen(),
                viaje.getLngOrigen(),
                viaje.getLatDestino(),
                viaje.getLngDestino()
        );
    }
}