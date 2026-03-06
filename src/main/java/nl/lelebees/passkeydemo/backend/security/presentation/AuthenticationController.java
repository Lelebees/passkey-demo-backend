package nl.lelebees.passkeydemo.backend.security.presentation;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.verifier.exception.VerificationException;
import nl.lelebees.passkeydemo.backend.security.application.AuthenticationService;
import nl.lelebees.passkeydemo.backend.security.application.dto.AuthenticationRequestOptionsDto;
import nl.lelebees.passkeydemo.backend.security.application.dto.ChallengeDto;
import nl.lelebees.passkeydemo.backend.security.application.dto.PublicKeyCredentialCreationOptionsDto;
import nl.lelebees.passkeydemo.backend.security.application.dto.UserCreationParametersDto;
import nl.lelebees.passkeydemo.backend.security.application.dto.jwt.AuthRefreshResponseDto;
import nl.lelebees.passkeydemo.backend.security.application.dto.jwt.AuthenticationResponseDto;
import nl.lelebees.passkeydemo.backend.security.application.exception.*;
import nl.lelebees.passkeydemo.backend.security.application.jwt.JwtToken;
import nl.lelebees.passkeydemo.backend.security.application.jwt.JwtUserDetails;
import nl.lelebees.passkeydemo.backend.user.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.user.domain.IncorrectEmailFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping("authentication/")
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
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
    public ResponseEntity<AuthenticationResponseDto> register(@RequestBody String data, @RequestHeader("User-Agent") String userAgent, @RequestHeader("session") String sessionId) {
        try {
            RegistrationData parsedData = webAuthnManager.parseRegistrationResponseJSON(data);
            return ResponseEntity.status(CREATED).body(service.registerUser(parsedData, userAgent, sessionId));
        } catch (NoChallengeIssuedException e) {
            throw new ResponseStatusException(NOT_FOUND, "No challenge found for session %s".formatted(sessionId));
        } catch (ChallengeExpiredException e) {
            throw new ResponseStatusException(GONE, "Challenge issued to %s expired before registration completed.".formatted(sessionId));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "No created user attached to this session. You should either POST to this URI to create one or attempt to log in instead.");
        } catch (DataConversionException e) {
            service.cancelSession(sessionId);
            throw new ResponseStatusException(BAD_REQUEST, "Could not parse registration data. Registration attempt has been canceled.");
        } catch (IncorrectEmailFormatException e) {
            throw new ResponseStatusException(MULTI_STATUS, "200 OK: Registration succeeded.\n500 INTERNAL SERVER ERROR: Access and refresh tokens could not be minted due to an e-mail formatting error. If you encounter this error, contact the server administrator. You may attempt logging in but you will probably find the same error occurring.");
        }
    }

    @DeleteMapping("/register")
    public ResponseEntity<String> cancelRegister(@RequestHeader("session") String sessionId) {
        service.cancelSession(sessionId);
        return ResponseEntity.ok("Canceled registration attempt. Challenge has been revoked.");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationRequestOptionsDto> startLogin() {
        return ResponseEntity.ok(service.startAuthentication());
    }

    @PatchMapping(value = "/login", consumes = "application/json")
    public ResponseEntity<AuthenticationResponseDto> authenticate(@RequestBody String data, @RequestHeader("session") String sessionId, @RequestHeader("User-Agent") String userAgent /* TODO: Log login attempts*/) {
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
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Passkey is registered but has no owner.");
        } catch (IncorrectEmailFormatException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Access and refresh tokens could not be minted due to an e-mail formatting error. If you encounter this error, contact the server administrator.");
        }
    }

    @DeleteMapping("/login")
    public ResponseEntity<String> cancelLogin(@RequestHeader("session") String sessionId) {
        service.cancelSession(sessionId);
        return ResponseEntity.ok("Canceled login attempt. Challenge has been revoked.");
    }

    @PostMapping("/add-key")
    public ResponseEntity<?> addKey(@AuthenticationPrincipal JwtUserDetails userDetails) {
        return ResponseEntity.ok(1);
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthRefreshResponseDto> refreshAccessToken(@RequestBody JwtToken refreshToken) {
        try {
            return ResponseEntity.ok(service.refreshAccessToken(refreshToken));
        } catch (InvalidTokenException | UserNotFoundException | IncorrectEmailFormatException e) {
            throw new ResponseStatusException(UNAUTHORIZED, "Refresh token not valid.");
        }
    }

    @DeleteMapping("/refresh")
    public ResponseEntity<String> signOut(@AuthenticationPrincipal JwtUserDetails userDetails) {
        try {
            service.signOut(userDetails.getId());
            return ResponseEntity.ok("Okay. Good-bye!");
        } catch (UserNotFoundException e) {
            log.warn("Access token valid but user not found.", e);
            throw new ResponseStatusException(NOT_FOUND, "User not found, but access token was valid. You do not need to do anything else.");
        }
    }
}
