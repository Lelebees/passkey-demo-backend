package nl.lelebees.passkeydemo.backend.presentation;

import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.verifier.exception.VerificationException;
import nl.lelebees.passkeydemo.backend.application.AuthenticationService;
import nl.lelebees.passkeydemo.backend.application.dto.AuthenticationResponse;
import nl.lelebees.passkeydemo.backend.application.dto.ChallengeDto;
import nl.lelebees.passkeydemo.backend.application.dto.PublicKeyCredentialCreationOptionsDto;
import nl.lelebees.passkeydemo.backend.application.dto.UserCreationParametersDto;
import nl.lelebees.passkeydemo.backend.application.exception.*;
import nl.lelebees.passkeydemo.backend.domain.IncorrectEmailFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping("authentication/")
public class AuthenticationController {

    private final AuthenticationService service;

    @Autowired
    public AuthenticationController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/challenge")
    public ResponseEntity<ChallengeDto> generateChallenge() {
        return ResponseEntity.ok(service.generateChallenge());
    }

    @PatchMapping(value = "/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegistrationData data, @RequestHeader("User-Agent") String userAgent, @RequestHeader("session") String sessionId) {
        try {
            return ResponseEntity.ok(service.registerUser(data, userAgent, sessionId));
        } catch (NoChallengeIssuedException e) {
            throw new ResponseStatusException(NOT_FOUND, "No challenge found for session %s".formatted(sessionId));
        } catch (ChallengeExpiredException e) {
            throw new ResponseStatusException(GONE, "Challenge issued to %s expired before registration completed.".formatted(sessionId));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "No created user attached to this session. You should either POST to this URI to create one or attempt to log in instead.");
        }
    }

    @PatchMapping(value = "/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationData data, @RequestHeader("session") String sessionId) {
        try {
            return ResponseEntity.ok(service.authenticateUser(data, sessionId));
        } catch (PasskeyNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "Passkey could not be found.");
        } catch (ChallengeExpiredException e) {
            throw new ResponseStatusException(GONE, "Issued value expired before authentication.");
        } catch (VerificationException e) {
            throw new ResponseStatusException(UNAUTHORIZED, "Incorrect signature");
        } catch (NoChallengeIssuedException e) {
            throw new ResponseStatusException(NOT_FOUND, "No challenge found for session %s".formatted(sessionId));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<PublicKeyCredentialCreationOptionsDto> generateOptions(@RequestBody UserCreationParametersDto params) {
        try {
            return ResponseEntity.ok(service.startRegistration(params));
        } catch (IncorrectEmailFormatException e) {
            throw new ResponseStatusException(UNPROCESSABLE_CONTENT, "%s is not in a valid email format".formatted(params.email()));
        } catch (EmailAlreadyRegisteredException e) {
            throw new ResponseStatusException(CONFLICT, "E-mail address already registered. Authenticate or reset passkey instead.");
        }
    }

    @DeleteMapping("/register")
    public ResponseEntity<String> cancelRegister(@RequestHeader("session") String sessionId) {
        service.cancelSession(sessionId);
        return ResponseEntity.ok("Canceled registration attempt. Challenge has been revoked.");
    }
}
