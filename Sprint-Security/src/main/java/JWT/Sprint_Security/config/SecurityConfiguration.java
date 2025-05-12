package JWT.Sprint_Security.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration // This class defines beans that contribute to the application context
@EnableWebSecurity // Enables Spring Security's default web security features
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider; // Custom authentication logic (e.g. how users are authenticated)
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // Custom filter to extract and validate JWT from requests

    // Constructor injection: Spring will automatically provide the required beans
    public SecurityConfiguration(AuthenticationProvider authenticationProvider, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Core Spring Security configuration: defines how HTTP requests are secured
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // Disable CSRF protection because we're using JWTs instead of cookies or sessions
            .csrf(csrf -> csrf.disable())

            // Define URL-based access rules
            .authorizeHttpRequests(authorize -> authorize
                // Allow anyone to access any endpoint that starts with "/auth/" (used for login, registration, etc.)
                .requestMatchers("/auth/**").permitAll()

                // Require authentication for all other endpoints
                .anyRequest().authenticated()
            )

            // Stateless session management: Spring Security will not create or use an HTTP session
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Tell Spring Security to use our custom authentication provider
            .authenticationProvider(authenticationProvider)

            // Add our JWT filter *before* the default username/password auth filter.
            // This ensures our filter runs first, extracting the JWT and setting the user in the security context if valid.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Return the fully configured security filter chain to the framework
        return http.build();
    }

    /**
     * Defines CORS (Cross-Origin Resource Sharing) policy to allow requests from other origins
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Define which client origins are allowed to make cross-origin requests
        // Replace these URLs with your actual frontend origin(s)
        configuration.setAllowedOrigins(List.of("https://app-backend.com", "http://localhost:8080"));

        // Define which HTTP methods are allowed from those origins
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));

        // Define which headers can be sent in requests from those origins
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Apply the above CORS settings to all URL paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
