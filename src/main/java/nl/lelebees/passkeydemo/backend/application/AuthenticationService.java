package nl.lelebees.passkeydemo.backend.application;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.verifier.exception.UserNotVerifiedException;
import com.webauthn4j.verifier.exception.VerificationException;
import nl.lelebees.passkeydemo.backend.application.dto.AuthenticationResponse;
import nl.lelebees.passkeydemo.backend.application.dto.ChallengeDto;
import nl.lelebees.passkeydemo.backend.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.application.exception.ChallengeExpiredException;
import nl.lelebees.passkeydemo.backend.application.exception.EmailAlreadyRegisteredException;
import nl.lelebees.passkeydemo.backend.application.exception.NoChallengeIssuedException;
import nl.lelebees.passkeydemo.backend.application.exception.PasskeyNotFoundException;
import nl.lelebees.passkeydemo.backend.data.ChallengeRepository;
import nl.lelebees.passkeydemo.backend.data.PasskeyRepository;
import nl.lelebees.passkeydemo.backend.domain.ChallengeEntity;
import nl.lelebees.passkeydemo.backend.domain.Email;
import nl.lelebees.passkeydemo.backend.domain.Passkey;
import nl.lelebees.passkeydemo.backend.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final String rpId;
    private final Origin origin;
    private final ChallengeRepository challengeRepository;
    private final PasskeyRepository passkeyRepository;
    private final WebAuthnManager webAuthnManager;

    @Autowired
    public AuthenticationService(UserService userService, JwtUtils jwtUtils, @Value("${webauthn.rp.id}") String rpId, @Value("${webauthn.origin.url}") String originUrl, ChallengeRepository challengeRepository, PasskeyRepository passkeyRepository) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.rpId = rpId;
        this.challengeRepository = challengeRepository;
        this.origin = new Origin(originUrl);
        this.passkeyRepository = passkeyRepository;
        this.webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
    }

    public AuthenticationResponse registerUser(RegistrationData data, Email email, String userAgent, String sessionId) throws ChallengeExpiredException, NoChallengeIssuedException, EmailAlreadyRegisteredException {
        ChallengeEntity challenge = findBySession(sessionId);
        ServerProperty server = ServerProperty.builder()
                .origin(origin)
                .rpId(rpId)
                .challenge(challenge)
                .build();
        RegistrationParameters parameters = new RegistrationParameters(server, null, false, true);
        RegistrationData verifiedData;
        try {
            verifiedData = webAuthnManager.verify(data, parameters);
        } catch (VerificationException e) {
            throw new UserNotVerifiedException("Could not register user", e);
        }
        challengeRepository.delete(challenge);
        UserDto user = userService.createUser(email, Passkey.From(userAgent, verifiedData));
        return new AuthenticationResponse(jwtUtils.generateJwtToken(user.email()));
    }

    public AuthenticationResponse authenticateUser(AuthenticationData data, String sessionId) throws PasskeyNotFoundException, ChallengeExpiredException, NoChallengeIssuedException {
        ChallengeEntity issuedChallenge = findBySession(sessionId);
        Passkey passkey = passkeyRepository.findById(data.getCredentialId()).orElseThrow(PasskeyNotFoundException::new);
        ServerProperty serverProperty = ServerProperty.builder()
                .origin(origin)
                .rpId(rpId)
                .challenge(issuedChallenge)
                .build();
        AuthenticationParameters authenticationParameters = new AuthenticationParameters(
                serverProperty,
                passkey.getCredentialRecord(),
                null,
                true,
                true
        );
        passkey.getCredentialRecord().setCounter(webAuthnManager.verify(data, authenticationParameters).getAuthenticatorData().getSignCount());
        passkeyRepository.save(passkey);
        challengeRepository.delete(issuedChallenge);
        return new AuthenticationResponse(jwtUtils.generateJwtToken(passkey.getOwnerEmail().toString()));
    }

    private ChallengeEntity findBySession(String sessionId) throws NoChallengeIssuedException, ChallengeExpiredException {
        ChallengeEntity entity = challengeRepository.findById(sessionId).orElseThrow(NoChallengeIssuedException::new);
        if (entity.isExpired()) {
            challengeRepository.delete(entity);
            throw new ChallengeExpiredException("Challenge expired at %s".formatted(entity.getExpires()));
        }
        return entity;
    }

    public ChallengeDto generateChallenge() {
        return ChallengeDto.From(challengeRepository.save(ChallengeEntity.randomChallenge()));
    }
}
