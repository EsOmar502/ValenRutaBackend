package progresa.proyectovalenruta.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import progresa.proyectovalenruta.Service.UsuarioService;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(UsuarioService usuarioService) {
        return new JwtAuthFilter(jwtUtil, usuarioService);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            UsuarioService usuarioService
    ) throws Exception {

        http
                .cors(org.springframework.security.config.Customizer.withDefaults())

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // SWAGGER
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // AUTH
                        .requestMatchers("/api/auth/**").permitAll()

                        // VIAJES GET público
                        .requestMatchers(HttpMethod.GET, "/api/viajes/**").permitAll()

                        // resto autenticado
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthFilter(usuarioService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}