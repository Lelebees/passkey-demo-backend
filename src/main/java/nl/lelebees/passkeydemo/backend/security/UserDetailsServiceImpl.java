package nl.lelebees.passkeydemo.backend.security;

import nl.lelebees.passkeydemo.backend.data.UserRepository;
import nl.lelebees.passkeydemo.backend.domain.Email;
import nl.lelebees.passkeydemo.backend.domain.IncorrectEmailFormatException;
import nl.lelebees.passkeydemo.backend.domain.User;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository repository;

    public UserDetailsServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        try {
            Optional<User> userOptional = repository.findUserByEmail(new Email(username));
            if (userOptional.isEmpty()) {
                throw new UsernameNotFoundException("User known as %s not found.");
            }
            return new UserDetailsImpl(userOptional.get());
        } catch (IncorrectEmailFormatException e) {
            throw new UsernameNotFoundException("User known as %s not found, invalid email format".formatted(username), e);
        }
    }
}
