package nl.lelebees.passkeydemo.backend.user.application;

import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;
import com.webauthn4j.data.RegistrationData;
import nl.lelebees.passkeydemo.backend.security.application.dto.UserCreationParametersDto;
import nl.lelebees.passkeydemo.backend.security.application.exception.EmailAlreadyRegisteredException;
import nl.lelebees.passkeydemo.backend.security.application.jwt.JwtToken;
import nl.lelebees.passkeydemo.backend.user.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.user.application.dto.UserOverviewDto;
import nl.lelebees.passkeydemo.backend.user.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.user.data.UserRepository;
import nl.lelebees.passkeydemo.backend.user.domain.Email;
import nl.lelebees.passkeydemo.backend.user.domain.IncorrectEmailFormatException;
import nl.lelebees.passkeydemo.backend.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.blueconic.browscap.BrowsCapField.BROWSER;
import static com.blueconic.browscap.BrowsCapField.PLATFORM;

@Service
public class UserService {

    private final UserRepository repository;
    private final UserAgentParser userAgentParser;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
        try {
            this.userAgentParser = new UserAgentService().loadParser(List.of(BROWSER, PLATFORM));
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public UserDto getUserById(UUID id) throws UserNotFoundException {
        return UserDto.from(getFromOptional(repository.findById(id)));
    }

    public UserDto getUserByEmail(Email email) throws UserNotFoundException {
        return UserDto.from(getFromOptional(repository.findUserByEmail(email)));
    }

    private User getFromOptional(Optional<User> opt) throws UserNotFoundException {
        return opt.orElseThrow(UserNotFoundException::new);
    }

    public UserOverviewDto createUser(UserCreationParametersDto parameters) throws EmailAlreadyRegisteredException, IncorrectEmailFormatException {
        Email email = new Email(parameters.email());
        if (repository.existsUserByEmail(email)) {
            throw new EmailAlreadyRegisteredException("%s is already registered.");
        }
        return UserOverviewDto.from(repository.save(new User(email, parameters.displayName())));
    }

    public void deleteUser(UUID userId) {
        repository.deleteById(userId);
    }

    public UserDto registerPasskey(UUID createdUser, String userAgent, RegistrationData verifiedData) throws UserNotFoundException {
        User user = getFromOptional(repository.findById(createdUser));
        user.registerKey(userAgent, verifiedData, userAgentParser.parse(userAgent));
        return UserDto.from(repository.save(user));
    }

    public void registerRefreshToken(String username, JwtToken token) throws IncorrectEmailFormatException, UserNotFoundException {
        User user = getFromOptional(repository.findUserByEmail(new Email(username)));
        user.registerRefreshToken(token);
        repository.save(user);
    }

    public boolean isRefreshTokenRetracted(Email email, JwtToken refreshToken) throws UserNotFoundException {
        return !getFromOptional(repository.findUserByEmail(email))
                .getAcceptedRefreshTokens().contains(refreshToken.toString());
    }
}
