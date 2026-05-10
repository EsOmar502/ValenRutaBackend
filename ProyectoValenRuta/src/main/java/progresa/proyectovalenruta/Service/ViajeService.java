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
import progresa.proyectovalenruta.Geolocalizacion.GeoService;
import progresa.proyectovalenruta.DTO.ViajeCercanoDTO;

import java.time.LocalDate;
import java.util.List;

@Service
public class ViajeService {

    private final ViajeDAO viajeDAO;
    private final ConductorDAO conductorDAO;
    private final GeoService geoService;

    public ViajeService(ViajeDAO viajeDAO, ConductorDAO conductorDAO, GeoService geoService) {
        this.viajeDAO = viajeDAO;
        this.conductorDAO = conductorDAO;
        this.geoService = geoService;
    }

    public List<Viaje> getAll() {
        return viajeDAO.findAll();
    }

    public Viaje getById(Long id) {
        return viajeDAO.findById(id).orElse(null);
    }

    public List<Viaje> buscarDisponibles(String origen, String destino, LocalDate fecha) {
        return viajeDAO.buscarDisponibles(origen, destino, fecha);
    }

    public Viaje save(Viaje viaje) {

        try {

            // 🔹 Validación conductor
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

            // 🔹 Limpieza básica de strings
            String origen = viaje.getOrigen() != null ? viaje.getOrigen().trim() : null;
            String destino = viaje.getDestino() != null ? viaje.getDestino().trim() : null;

            viaje.setOrigen(origen);
            viaje.setDestino(destino);

            // 🔹 Validar duplicados
            boolean existe = viajeDAO.existsByOrigenAndDestinoAndFechaAndConductor_Id(
                    origen,
                    destino,
                    viaje.getFecha(),
                    conductor.getId()
            );

            if (existe) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ya existe un viaje con la misma información para este conductor."
                );
            }

            // 🔥 GEOLOCALIZACIÓN INTELIGENTE (PRO)
            // Si el frontend ya manda coords → NO llamar a Google
            if (viaje.getOrigenLat() == null || viaje.getOrigenLng() == null) {

                double[] coords = geoService.getCoordinates(origen);

                if (coords == null || coords.length < 2) {
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "No se pudieron obtener coordenadas"
                    );
                }

                viaje.setOrigenLat(coords[0]);
                viaje.setOrigenLng(coords[1]);

            }

            viaje.setConductor(conductor);
            viaje.setEstado("PROGRAMADO");

            return viajeDAO.save(viaje);

        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Intento de duplicado detectado."
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error creando viaje: " + e.getMessage()
            );
        }
    }

    public void delete(Long id) {
        viajeDAO.deleteById(id);
    }

    // 🔥 MÉTODO PRO: DTO + distancia + ordenado
    public List<ViajeCercanoDTO> buscarCercanos(Double lat, Double lng, Double radioKm) {

        return viajeDAO.findAll().stream()
                .filter(v -> v.getOrigenLat() != null && v.getOrigenLng() != null)
                .map(v -> {

                    double distancia = calcularDistancia(
                            lat,
                            lng,
                            v.getOrigenLat(),
                            v.getOrigenLng()
                    );

                    if (distancia <= radioKm) {
                        ViajeCercanoDTO dto = new ViajeCercanoDTO();
                        dto.setId(v.getId());
                        dto.setOrigen(v.getOrigen());
                        dto.setDestino(v.getDestino());
                        dto.setPrecio(v.getPrecio());
                        dto.setDistanciaKm(Math.round(distancia * 100.0) / 100.0);
                        return dto;
                    }

                    return null;
                })
                .filter(v -> v != null)
                .sorted((a, b) -> Double.compare(a.getDistanciaKm(), b.getDistanciaKm()))
                .toList();
    }

    // 🔥 Fórmula Haversine (correcta)
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
                .map(viaje -> new ViajeDisponibleDTO(

                        viaje.getId(),
                        viaje.getOrigen(),
                        viaje.getDestino(),
                        viaje.getFecha().toString(),
                        viaje.getPrecio(),
                        viaje.getAsientosDisponibles(),
                        viaje.getConductor().getUsuario().getNombre()
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
                viaje.getFecha().toString(),
                viaje.getPrecio(),
                viaje.getAsientosDisponibles(),
                viaje.getEstado(),
                viaje.getConductor().getUsuario().getNombre()
        );
    }
}