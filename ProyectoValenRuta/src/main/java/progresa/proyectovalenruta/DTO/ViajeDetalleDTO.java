package progresa.proyectovalenruta.DTO;

public class ViajeDetalleDTO {

    private Long id;
    private String origen;
    private String destino;
    private String fechaSalida;
    private Double precio;
    private Integer asientosDisponibles;
    private String estado;
    private String conductorNombre;

    private Double latOrigen;
    private Double lngOrigen;
    private Double latDestino;
    private Double lngDestino;

    public ViajeDetalleDTO(
            Long id,
            String origen,
            String destino,
            String fechaSalida,
            Double precio,
            Integer asientosDisponibles,
            String estado,
            String conductorNombre,
            Double latOrigen,
            Double lngOrigen,
            Double latDestino,
            Double lngDestino
    ) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.fechaSalida = fechaSalida;
        this.precio = precio;
        this.asientosDisponibles = asientosDisponibles;
        this.estado = estado;
        this.conductorNombre = conductorNombre;
        this.latOrigen = latOrigen;
        this.lngOrigen = lngOrigen;
        this.latDestino = latDestino;
        this.lngDestino = lngDestino;
    }

    public Long getId() { return id; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public String getFechaSalida() { return fechaSalida; }
    public Double getPrecio() { return precio; }
    public Integer getAsientosDisponibles() { return asientosDisponibles; }
    public String getEstado() { return estado; }
    public String getConductorNombre() { return conductorNombre; }
    public Double getLatOrigen() { return latOrigen; }
    public Double getLngOrigen() { return lngOrigen; }
    public Double getLatDestino() { return latDestino; }
    public Double getLngDestino() { return lngDestino; }
}