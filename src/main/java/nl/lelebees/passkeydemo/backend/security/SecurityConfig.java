package nl.lelebees.passkeydemo.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .formLogin(withDefaults())
                .webAuthn((webAuthn) -> webAuthn
                        .rpId("localhost")
                        .allowedOrigins("http://localhost")
                );
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        UserDetails userDetails = User.withUsername("user")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(userDetails);
    }
}
