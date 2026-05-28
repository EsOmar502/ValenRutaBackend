package progresa.proyectovalenruta.DTO;


public class ViajeDisponibleDTO {

    private Long id;
    private String origen;
    private String destino;
    private String fechaSalida;
    private Double precio;
    private Integer asientosDisponibles;
    private String conductorNombre;
    private Long conductorId;

    private Double latOrigen;
    private Double lngOrigen;
    private Double latDestino;
    private Double lngDestino;
    private String estado;
    private boolean completo;
    private boolean puedeIniciar;
    private boolean puedeFinalizar;

    public ViajeDisponibleDTO() {
    }

    public ViajeDisponibleDTO(
            Long id,
            String origen,
            String destino,
            String fechaSalida,
            Double precio,
            Integer asientosDisponibles,
            String conductorNombre,
            Long conductorId,
            Double latOrigen,
            Double lngOrigen,
            Double latDestino,
            Double lngDestino,
            String estado,
            boolean puedeIniciar,
            boolean puedeFinalizar
    ) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.fechaSalida = fechaSalida;
        this.precio = precio;
        this.asientosDisponibles = asientosDisponibles;
        this.conductorNombre = conductorNombre;
        this.conductorId = conductorId;
        this.latOrigen = latOrigen;
        this.lngOrigen = lngOrigen;
        this.latDestino = latDestino;
        this.lngDestino = lngDestino;
        this.estado = estado;
        this.completo = (asientosDisponibles != null && asientosDisponibles <= 0);
        this.puedeIniciar = puedeIniciar;
        this.puedeFinalizar = puedeFinalizar;
    }

    public Long getId() { return id; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public String getFechaSalida() { return fechaSalida; }
    public Double getPrecio() { return precio; }
    public Integer getAsientosDisponibles() { return asientosDisponibles; }
    public String getConductorNombre() { return conductorNombre; }
    public Long getConductorId() { return conductorId; }
    public Double getLatOrigen() { return latOrigen; }
    public Double getLngOrigen() { return lngOrigen; }
    public Double getLatDestino() { return latDestino; }
    public Double getLngDestino() { return lngDestino; }

    public void setId(Long id) { this.id = id; }
    public void setOrigen(String origen) { this.origen = origen; }
    public void setDestino(String destino) { this.destino = destino; }
    public void setFechaSalida(String fechaSalida) { this.fechaSalida = fechaSalida; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public void setAsientosDisponibles(Integer asientosDisponibles) { this.asientosDisponibles = asientosDisponibles; }
    public void setConductorNombre(String conductorNombre) { this.conductorNombre = conductorNombre; }
    public void setConductorId(Long conductorId) { this.conductorId = conductorId; }
    public void setLatOrigen(Double latOrigen) { this.latOrigen = latOrigen; }
    public void setLngOrigen(Double lngOrigen) { this.lngOrigen = lngOrigen; }
    public void setLatDestino(Double latDestino) { this.latDestino = latDestino; }
    public void setLngDestino(Double lngDestino) { this.lngDestino = lngDestino; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isCompleto() { return completo; }
    public void setCompleto(boolean completo) { this.completo = completo; }
    public boolean isPuedeIniciar() { return puedeIniciar; }
    public void setPuedeIniciar(boolean puedeIniciar) { this.puedeIniciar = puedeIniciar; }
    public boolean isPuedeFinalizar() { return puedeFinalizar; }
    public void setPuedeFinalizar(boolean puedeFinalizar) { this.puedeFinalizar = puedeFinalizar; }
}