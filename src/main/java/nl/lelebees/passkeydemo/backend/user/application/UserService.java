package nl.lelebees.passkeydemo.backend.user.application;

import com.webauthn4j.data.RegistrationData;
import nl.lelebees.passkeydemo.backend.security.application.dto.UserCreationParametersDto;
import nl.lelebees.passkeydemo.backend.user.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.security.application.exception.EmailAlreadyRegisteredException;
import nl.lelebees.passkeydemo.backend.user.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.user.data.UserRepository;
import nl.lelebees.passkeydemo.backend.user.domain.Email;
import nl.lelebees.passkeydemo.backend.user.domain.IncorrectEmailFormatException;
import nl.lelebees.passkeydemo.backend.user.domain.Passkey;
import nl.lelebees.passkeydemo.backend.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserDto getUserById(UUID id) throws UserNotFoundException {
        return UserDto.From(getFromOptional(repository.findById(id)));
    }

    public UserDto getUserByEmail(Email email) throws UserNotFoundException {
        return UserDto.From(getFromOptional(repository.findUserByEmail(email)));
    }

    private User getFromOptional(Optional<User> opt) throws UserNotFoundException {
        return opt.orElseThrow(UserNotFoundException::new);
    }

    public UserDto createUser(Email email, Passkey passkey) throws EmailAlreadyRegisteredException {
        if (repository.existsUserByEmail(email)) {
            throw new EmailAlreadyRegisteredException("%s is already registered.");
        }
        return UserDto.From(repository.save(new User(email, passkey)));
    }

    public UserDto createUser(UserCreationParametersDto parameters) throws EmailAlreadyRegisteredException, IncorrectEmailFormatException {
        Email email = new Email(parameters.email());
        if (repository.existsUserByEmail(email)) {
            throw new EmailAlreadyRegisteredException("%s is already registered.");
        }
        return UserDto.From(repository.save(new User(email, parameters.displayName())));
    }

    public UserDto registerPasskey(UUID createdUser, Passkey passkey) throws UserNotFoundException {
        User user = getFromOptional(repository.findById(createdUser));
        user.registerKey(passkey);;
        return UserDto.From(repository.save(user));
    }

    public void deleteUser(UUID userId) {
        repository.deleteById(userId);
    }

    public UserDto registerPasskey(UUID createdUser, String userAgent, RegistrationData verifiedData) throws UserNotFoundException {
        User user = getFromOptional(repository.findById(createdUser));
        user.registerKey(userAgent, verifiedData);
        return UserDto.From(repository.save(user));
    }
}
