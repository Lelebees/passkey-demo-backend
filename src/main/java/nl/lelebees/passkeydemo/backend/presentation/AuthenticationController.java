package nl.lelebees.passkeydemo.backend.presentation;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.data.*;
import com.webauthn4j.verifier.exception.VerificationException;
import nl.lelebees.passkeydemo.backend.application.AuthenticationService;
import nl.lelebees.passkeydemo.backend.application.UserService;
import nl.lelebees.passkeydemo.backend.application.dto.AuthenticationResponse;
import nl.lelebees.passkeydemo.backend.application.dto.UserAuthenticationParametersDto;
import nl.lelebees.passkeydemo.backend.application.dto.UserCreationParametersDto;
import nl.lelebees.passkeydemo.backend.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.application.exception.ChallengeExpiredException;
import nl.lelebees.passkeydemo.backend.application.exception.EmailAlreadyRegisteredException;
import nl.lelebees.passkeydemo.backend.application.exception.PasskeyNotFoundException;
import nl.lelebees.passkeydemo.backend.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.domain.Email;
import nl.lelebees.passkeydemo.backend.domain.IncorrectEmailFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping("authentication/")
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService service;
    private final WebAuthnManager webAuthnManager;

    public AuthenticationController(UserService userService, AuthenticationService service, WebAuthnManager webAuthnManager) {
        this.userService = userService;
        this.service = service;
        this.webAuthnManager = webAuthnManager;
    }

    @Autowired
    public AuthenticationController(UserService userService, AuthenticationService service) {
        this.userService = userService;
        this.service = service;
        this.webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
    }

    @PostMapping("/register")
    public PublicKeyCredentialCreationOptions register(@RequestBody UserCreationParametersDto userOptions) {
        try {
            return service.startRegisterProcess(userOptions);
        } catch (IncorrectEmailFormatException e) {
            throw new ResponseStatusException(UNPROCESSABLE_CONTENT, "%s is not in a valid email format".formatted(userOptions.email()));
        } catch (EmailAlreadyRegisteredException e) {
            throw new ResponseStatusException(CONFLICT, "Email address already registered. Reset passkey instead.");
        }
    }

    @PatchMapping(value = "/register", consumes = "application/json")
    public AuthenticationResponse uploadKey(@RequestBody String data, @RequestParam Email email, @RequestHeader("User-Agent") String userAgent) {
        RegistrationData registrationData;
        try {
            registrationData = webAuthnManager.parseRegistrationResponseJSON(data);
        } catch (DataConversionException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Could not parse registration data");
        }
        try {
            return service.registerUser(registrationData, email, userAgent);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "User with email address %s not found.".formatted(email));
        } catch (ChallengeExpiredException e) {
            throw new ResponseStatusException(GONE, "Issued challenge expired before verification.");
        }
    }

    @PostMapping("/login")
    public PublicKeyCredentialRequestOptions login(@RequestBody UserAuthenticationParametersDto dto) {
        try {
            return service.startAuthenticationProcess(dto);
        } catch (IncorrectEmailFormatException e) {
            throw new ResponseStatusException(UNPROCESSABLE_CONTENT, "%s is not a valid email address.".formatted(dto.email()));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "User %s could not be found.".formatted(dto.email()));
        }
    }

    @PatchMapping(value = "/login", consumes = "application/json")
    public AuthenticationResponse authenticateKey(@RequestBody String data) {
        AuthenticationData authenticationData;
        try {
            authenticationData = webAuthnManager.parseAuthenticationResponseJSON(data);
        } catch (DataConversionException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Could not process authentication data");
        }
        try {
            return service.authenticateUser(authenticationData);
        } catch (PasskeyNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "Passkey could not be found.");
        } catch (ChallengeExpiredException e) {
            throw new ResponseStatusException(GONE, "Issued challenge expired before authentication.");
        } catch (VerificationException e) {
            throw new ResponseStatusException(UNAUTHORIZED, "Incorrect signature");
        }
    }

    @GetMapping("/users/{id}")
    public UserDto getUser(@PathVariable UUID id) {
        try {
            return userService.getUserById(id);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "User with id %s not found.".formatted(id));
        }
    }
}
