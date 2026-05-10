package progresa.proyectovalenruta.DTO;

public class ViajeDetalleDTO {

    private Long id;
    private String origen;
    private String destino;
    private String fecha;
    private Double precio;
    private Integer asientosDisponibles;
    private String estado;
    private String conductorNombre;

    public ViajeDetalleDTO(
            Long id,
            String origen,
            String destino,
            String fecha,
            Double precio,
            Integer asientosDisponibles,
            String estado,
            String conductorNombre
    ) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.fecha = fecha;
        this.precio = precio;
        this.asientosDisponibles = asientosDisponibles;
        this.estado = estado;
        this.conductorNombre = conductorNombre;
    }

    public Long getId() {
        return id;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public String getFecha() {
        return fecha;
    }

    public Double getPrecio() {
        return precio;
    }

    public Integer getAsientosDisponibles() {
        return asientosDisponibles;
    }

    public String getEstado() {
        return estado;
    }

    public String getConductorNombre() {
        return conductorNombre;
    }
}