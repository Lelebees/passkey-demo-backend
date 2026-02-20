package nl.lelebees.passkeydemo.backend.presentation;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import com.webauthn4j.data.RegistrationData;
import nl.lelebees.passkeydemo.backend.application.AuthenticationService;
import nl.lelebees.passkeydemo.backend.application.UserService;
import nl.lelebees.passkeydemo.backend.application.dto.UserCreationParametersDto;
import nl.lelebees.passkeydemo.backend.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.application.exception.EmailAlreadyRegisteredException;
import nl.lelebees.passkeydemo.backend.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.domain.IncorrectEmailFormatException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Controller("/authentication")
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService service;

    public AuthenticationController(UserService userService, AuthenticationService service) {
        this.userService = userService;
        this.service = service;
    }

    @PostMapping("/register")
    public PublicKeyCredentialCreationOptions register(UserCreationParametersDto userOptions) {
        try {
            return service.startRegisterProcess(userOptions);
        } catch (IncorrectEmailFormatException e) {
            throw new ResponseStatusException(UNPROCESSABLE_CONTENT, "%s is not in a valid email format".formatted(userOptions.email()));
        } catch (EmailAlreadyRegisteredException e) {
            throw new ResponseStatusException(CONFLICT, "Email address already registered. Reset passkey instead.");
        }
    }

    @PatchMapping(value = "/register", consumes = "application/json")
    public void uploadKey(String data) {
        RegistrationData registrationData = WebAuthnManager.createNonStrictWebAuthnManager().parseRegistrationResponseJSON(data);

    }

    @GetMapping("/users/{id}")
    public UserDto getUser(@PathVariable UUID id) {
        try {
            return userService.getUserById(id);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "User %s could not be found".formatted(id));
        }
    }
}
