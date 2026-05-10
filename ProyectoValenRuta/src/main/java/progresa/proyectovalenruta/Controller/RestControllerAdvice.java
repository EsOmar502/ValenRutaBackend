package progresa.proyectovalenruta.Controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class RestControllerAdvice {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrity() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Ya existe un viaje con esa información.");
    }
}


