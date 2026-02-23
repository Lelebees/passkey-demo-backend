package nl.lelebees.passkeydemo.backend.presentation;

import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.verifier.exception.VerificationException;
import nl.lelebees.passkeydemo.backend.application.AuthenticationService;
import nl.lelebees.passkeydemo.backend.application.dto.AuthenticationResponse;
import nl.lelebees.passkeydemo.backend.application.dto.ChallengeDto;
import nl.lelebees.passkeydemo.backend.application.exception.ChallengeExpiredException;
import nl.lelebees.passkeydemo.backend.application.exception.EmailAlreadyRegisteredException;
import nl.lelebees.passkeydemo.backend.application.exception.NoChallengeIssuedException;
import nl.lelebees.passkeydemo.backend.application.exception.PasskeyNotFoundException;
import nl.lelebees.passkeydemo.backend.domain.Email;
import nl.lelebees.passkeydemo.backend.domain.IncorrectEmailFormatException;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ChallengeDto generateChallenge() {
        return service.generateChallenge();
    }

    @PostMapping(value = "/register")
    public AuthenticationResponse register(@RequestBody RegistrationData data, @RequestParam String email, @RequestHeader("User-Agent") String userAgent, @RequestHeader("session") String sessionId) {
        try {
            return service.registerUser(data, new Email(email), userAgent, sessionId);
        } catch (NoChallengeIssuedException e) {
            throw new ResponseStatusException(NOT_FOUND, "No challenge found for session %s".formatted(sessionId));
        } catch (ChallengeExpiredException e) {
            throw new ResponseStatusException(GONE, "Challenge issued to %s expired before registration completed.".formatted(sessionId));
        } catch (IncorrectEmailFormatException e) {
            throw new ResponseStatusException(UNPROCESSABLE_CONTENT, "%s is not in a valid email format".formatted(email));
        } catch (EmailAlreadyRegisteredException e) {
            throw new ResponseStatusException(CONFLICT, "E-mail address already registered. Authenticate or reset passkey instead.");
        }
    }

    @PostMapping(value = "/login")
    public AuthenticationResponse authenticate(@RequestBody AuthenticationData data, @RequestHeader("session") String sessionId) {
        try {
            return service.authenticateUser(data, sessionId);
        } catch (PasskeyNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "Passkey could not be found.");
        } catch (ChallengeExpiredException e) {
            throw new ResponseStatusException(GONE, "Issued challenge expired before authentication.");
        } catch (VerificationException e) {
            throw new ResponseStatusException(UNAUTHORIZED, "Incorrect signature");
        } catch (NoChallengeIssuedException e) {
            throw new ResponseStatusException(NOT_FOUND, "No challenge found for session %s".formatted(sessionId));
        }
    }
}
