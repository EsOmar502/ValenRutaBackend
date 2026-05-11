package progresa.proyectovalenruta;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Permitir localhost, IP local, ngrok, móvil, etc.
        config.addAllowedOriginPattern("*");

        // Headers permitidos
        config.setAllowedHeaders(List.of("*"));

        // Métodos permitidos
        config.setAllowedMethods(List.of("*"));

        // JWT / cookies / auth
        config.setAllowCredentials(true);

        // Exponer Authorization
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}