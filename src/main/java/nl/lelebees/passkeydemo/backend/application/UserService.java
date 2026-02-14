package nl.lelebees.passkeydemo.backend.application;

import nl.lelebees.passkeydemo.backend.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.data.UserRepository;
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
}
