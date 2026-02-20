package nl.lelebees.passkeydemo.backend.application;

import nl.lelebees.passkeydemo.backend.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.application.dto.UserCreationParametersDto;
import nl.lelebees.passkeydemo.backend.application.exception.EmailAlreadyRegisteredException;
import nl.lelebees.passkeydemo.backend.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.data.UserRepository;
import nl.lelebees.passkeydemo.backend.domain.Email;
import nl.lelebees.passkeydemo.backend.domain.IncorrectEmailFormatException;
import nl.lelebees.passkeydemo.backend.domain.ChallengeEntity;
import nl.lelebees.passkeydemo.backend.domain.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserDto getUserById(UUID id) throws UserNotFoundException {
        return UserDto.From(repository.findById(id).orElseThrow(UserNotFoundException::new));
    }

    public UserDto createUser(UserCreationParametersDto optionsDto, ChallengeEntity challenge) throws EmailAlreadyRegisteredException, IncorrectEmailFormatException {
        Email email = new Email(optionsDto.email());
        if (repository.existsUserByEmail(email))
        {
            throw new EmailAlreadyRegisteredException("%s is already registered.");
        }
        return UserDto.From(repository.save(new User(email, optionsDto.displayName(), challenge)));
    }

    public UserDto getUserByEmail(Email email) throws UserNotFoundException {
        return UserDto.From(repository.findUserByEmail(email).orElseThrow(UserNotFoundException::new));
    }
}
