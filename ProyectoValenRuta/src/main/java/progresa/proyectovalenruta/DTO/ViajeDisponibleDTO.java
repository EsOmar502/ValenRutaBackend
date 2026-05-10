package progresa.proyectovalenruta.DTO;


public class ViajeDisponibleDTO {

    private Long id;

    private String origen;

    private String destino;

    private String fecha;

    private Double precio;

    private Integer asientosDisponibles;

    private String conductorNombre;

    public ViajeDisponibleDTO() {
    }

    public ViajeDisponibleDTO(
            Long id,
            String origen,
            String destino,
            String fecha,
            Double precio,
            Integer asientosDisponibles,
            String conductorNombre
    ) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.fecha = fecha;
        this.precio = precio;
        this.asientosDisponibles = asientosDisponibles;
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

    public String getConductorNombre() {
        return conductorNombre;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public void setAsientosDisponibles(Integer asientosDisponibles) {
        this.asientosDisponibles = asientosDisponibles;
    }

    public void setConductorNombre(String conductorNombre) {
        this.conductorNombre = conductorNombre;
    }
}