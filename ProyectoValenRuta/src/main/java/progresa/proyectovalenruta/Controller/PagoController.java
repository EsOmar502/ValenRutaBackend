package progresa.proyectovalenruta.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import progresa.proyectovalenruta.DTO.PagoDTO;
import progresa.proyectovalenruta.Entity.Pago;
import progresa.proyectovalenruta.Entity.Usuario;
import progresa.proyectovalenruta.Entity.Viaje;
import progresa.proyectovalenruta.Service.PagoService;
import progresa.proyectovalenruta.Service.UsuarioService;
import progresa.proyectovalenruta.Service.ViajeService;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin("*")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ViajeService viajeService;

    @GetMapping
    public List<Pago> listar() {
        return pagoService.getAll();
    }

    @PostMapping
    public Pago crear(@RequestBody PagoDTO dto) {

        Usuario u = usuarioService.getById(dto.getUsuarioId());
        Viaje v = viajeService.getById(dto.getViajeId());

        Pago p = new Pago();
        p.setMonto(dto.getMonto());
        p.setFecha(dto.getFecha());
        p.setUsuario(u);
        p.setViaje(v);

        return pagoService.save(p);
    }
}
