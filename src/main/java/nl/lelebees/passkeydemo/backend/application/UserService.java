package nl.lelebees.passkeydemo.backend.application;

import nl.lelebees.passkeydemo.backend.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.application.exception.EmailAlreadyRegisteredException;
import nl.lelebees.passkeydemo.backend.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.data.UserRepository;
import nl.lelebees.passkeydemo.backend.domain.Email;
import nl.lelebees.passkeydemo.backend.domain.Passkey;
import nl.lelebees.passkeydemo.backend.domain.User;
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
}
