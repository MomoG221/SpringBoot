package JWT.Sprint_Security.config;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import JWT.Sprint_Security.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;

import org.springframework.web.filter.OncePerRequestFilter;

@Component // Register this class as a Spring Bean so it's used in the security filter chain
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver; // Handles exceptions thrown inside the filter
    private final JwtService jwtService; // My custom service to extract and validate JWT tokens
    private final UserDetailsService userDetailsService; // Loads user data from the database

    // Constructor-based dependency injection for the filter
    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Get the Authorization header (expected to contain the JWT token)
        final String authHeader = request.getHeader("Authorization");

        // If there's no token or the header doesn't start with "Bearer", skip this filter
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            filterChain.doFilter(request, response); // Let the request pass without authentication
            return;
        }

        try {
            // Extract the JWT token (strip "Bearer " prefix)
            final String jwt = authHeader.substring(7);

            // Use the JwtService to extract the username (email) from the token
            final String userEmail = jwtService.extractUsername(jwt);

            // Check if the user is already authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // If userEmail is present and no one is authenticated yet, proceed with validation
            if (userEmail != null && authentication == null) {
                // Load the user's details from the database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Validate the token against the user details (also checks if expired)
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Create an authentication object with the user details and authorities
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // Attach additional details like IP address, session info
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set the authenticated user into the security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // Continue the filter chain (request will be processed further)
            filterChain.doFilter(request, response);

        } catch (Exception exception) {
            // If any exception occurs during the token parsing/validation, delegate to global handler
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }
}
