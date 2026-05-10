package progresa.proyectovalenruta.Geolocalizacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GeoService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    public double[] getCoordinates(String direccion) {

        System.out.println("API KEY: " + apiKey); // 🔥 AQUÍ

        String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                + direccion.replace(" ", "%20")
                + "&key=" + apiKey;

        System.out.println("URL: " + url); // 🔥 MUY IMPORTANTE

        RestTemplate restTemplate = new RestTemplate();
        Map response = restTemplate.getForObject(url, Map.class);

        System.out.println("RESPONSE: " + response); // 🔥 CLAVE

        try {
            Map results = ((java.util.List<Map>) response.get("results")).get(0);
            Map geometry = (Map) results.get("geometry");
            Map location = (Map) geometry.get("location");

            double lat = (double) location.get("lat");
            double lng = (double) location.get("lng");

            return new double[]{lat, lng};

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 AHORA VERÁS EL ERROR REAL
            throw new RuntimeException("Error obteniendo coordenadas: " + e.getMessage());
        }
    }
}