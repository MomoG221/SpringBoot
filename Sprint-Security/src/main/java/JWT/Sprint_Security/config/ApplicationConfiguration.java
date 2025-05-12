package JWT.Sprint_Security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import JWT.Sprint_Security.repository.UserRepository;

@Configuration // Let Spring know this is a config class that defines beans
public class ApplicationConfiguration {

    private final UserRepository userRepository; // My JPA repo to fetch users by email from the DB

    // Constructor to inject the UserRepository (used later in userDetailsService)
    public ApplicationConfiguration(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Bean // Tells Spring to treat this method's return value as a bean it can manage
    UserDetailsService userDetailsService(){
        // This lambda tells Spring how to load users by username (email in this case)
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found")); // If no user is found, throw exception
    }

    @Bean // Registering a bean for password encoding
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use BCrypt to hash passwords (secure and recommended)
    }

    @Bean // This bean is needed to handle authentication (login)
    AuthenticationManager AuthenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // Ask Spring Security to give me the configured AuthenticationManager
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean // Tells Spring how to actually perform authentication
    AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(); // This uses my DB to check credentials

        authProvider.setUserDetailsService(userDetailsService()); // Use my custom user loader
        authProvider.setPasswordEncoder(passwordEncoder()); // Use BCrypt to compare passwords

        return authProvider; // Return the fully configured provider
    }
}
