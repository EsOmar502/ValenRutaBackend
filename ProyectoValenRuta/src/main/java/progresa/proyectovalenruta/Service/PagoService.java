package progresa.proyectovalenruta.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import progresa.proyectovalenruta.DAO.PagoDAO;
import progresa.proyectovalenruta.Entity.Pago;

import java.util.List;

@Service
public class PagoService {

    @Autowired
    private PagoDAO pagoDAO;

    public List<Pago> getAll() {
        return pagoDAO.findAll();
    }

    public Pago getById(Long id) {
        return pagoDAO.findById(id).orElse(null);
    }

    public Pago save(Pago p) {
        return pagoDAO.save(p);
    }

    public void delete(Long id) {
        pagoDAO.deleteById(id);
    }
}
