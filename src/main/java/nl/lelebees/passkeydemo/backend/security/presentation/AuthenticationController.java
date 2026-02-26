package nl.lelebees.passkeydemo.backend.security.presentation;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.verifier.exception.VerificationException;
import nl.lelebees.passkeydemo.backend.security.application.AuthenticationService;
import nl.lelebees.passkeydemo.backend.user.domain.IncorrectEmailFormatException;
import nl.lelebees.passkeydemo.backend.security.application.dto.*;
import nl.lelebees.passkeydemo.backend.security.application.exception.ChallengeExpiredException;
import nl.lelebees.passkeydemo.backend.security.application.exception.EmailAlreadyRegisteredException;
import nl.lelebees.passkeydemo.backend.security.application.exception.NoChallengeIssuedException;
import nl.lelebees.passkeydemo.backend.security.application.exception.PasskeyNotFoundException;
import nl.lelebees.passkeydemo.backend.user.application.exception.UserNotFoundException;
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
    private final WebAuthnManager webAuthnManager;

    @Autowired
    public AuthenticationController(AuthenticationService service) {
        this.service = service;
        this.webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
    }

    @PostMapping("/challenge")
    public ResponseEntity<ChallengeDto> generateChallenge() {
        return ResponseEntity.ok(service.generateChallenge());
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

    @PatchMapping(value = "/register", consumes = "application/json")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody String data, @RequestHeader("User-Agent") String userAgent, @RequestHeader("session") String sessionId) {
        try {
            RegistrationData parsedData = webAuthnManager.parseRegistrationResponseJSON(data);
            return ResponseEntity.ok(service.registerUser(parsedData, userAgent, sessionId));
        } catch (NoChallengeIssuedException e) {
            throw new ResponseStatusException(NOT_FOUND, "No challenge found for session %s".formatted(sessionId));
        } catch (ChallengeExpiredException e) {
            throw new ResponseStatusException(GONE, "Challenge issued to %s expired before registration completed.".formatted(sessionId));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "No created user attached to this session. You should either POST to this URI to create one or attempt to log in instead.");
        } catch (DataConversionException e) {
            service.cancelSession(sessionId);
            throw new ResponseStatusException(BAD_REQUEST, "Could not parse registration data. Registration attempt has been canceled.");
        }
    }

    @DeleteMapping("/register")
    public ResponseEntity<String> cancelRegister(@RequestHeader("session") String sessionId) {
        service.cancelSession(sessionId);
        return ResponseEntity.ok("Canceled registration attempt. Challenge has been revoked.");
    }

    @PostMapping(value = "/login")
    public ResponseEntity<AuthenticationRequestOptionsDto> startLogin() {
        return ResponseEntity.ok(service.startAuthentication());
    }

    @PatchMapping(value = "/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody String data, @RequestHeader("session") String sessionId, @RequestHeader("User-Agent") String userAgent /* TODO: Log login attempts*/) {
        try {
            AuthenticationData authenticationData = webAuthnManager.parseAuthenticationResponseJSON(data);
            return ResponseEntity.ok(service.authenticateUser(authenticationData, sessionId));
        } catch (PasskeyNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "Passkey could not be found.");
        } catch (ChallengeExpiredException e) {
            throw new ResponseStatusException(GONE, "Issued value expired before authentication.");
        } catch (VerificationException e) {
            throw new ResponseStatusException(UNAUTHORIZED, "Incorrect signature");
        } catch (NoChallengeIssuedException e) {
            throw new ResponseStatusException(NOT_FOUND, "No challenge found for session %s".formatted(sessionId));
        } catch (DataConversionException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Could not parse authentication response.");
        }
    }

    @DeleteMapping(value = "/login")
    public ResponseEntity<String> cancelLogin(@RequestHeader("session") String sessionId) {
        service.cancelSession(sessionId);
        return ResponseEntity.ok("Canceled login attempt. Challenge has been revoked.");
    }
}
