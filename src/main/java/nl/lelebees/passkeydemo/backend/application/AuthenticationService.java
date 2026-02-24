package nl.lelebees.passkeydemo.backend.application;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.*;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.verifier.exception.UserNotVerifiedException;
import com.webauthn4j.verifier.exception.VerificationException;
import nl.lelebees.passkeydemo.backend.application.dto.*;
import nl.lelebees.passkeydemo.backend.application.exception.*;
import nl.lelebees.passkeydemo.backend.data.ChallengeRepository;
import nl.lelebees.passkeydemo.backend.data.PasskeyRepository;
import nl.lelebees.passkeydemo.backend.domain.ChallengeEntity;
import nl.lelebees.passkeydemo.backend.domain.IncorrectEmailFormatException;
import nl.lelebees.passkeydemo.backend.domain.Passkey;
import nl.lelebees.passkeydemo.backend.domain.PublicKeyCredentialCreationOptionsBuilder;
import nl.lelebees.passkeydemo.backend.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.webauthn4j.data.AuthenticatorAttachment.*;
import static com.webauthn4j.data.PublicKeyCredentialHints.*;
import static com.webauthn4j.data.PublicKeyCredentialType.PUBLIC_KEY;
import static com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.*;

@Service
public class AuthenticationService {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final String rpId;
    private final Origin origin;
    private final ChallengeRepository challengeRepository;
    private final PasskeyRepository passkeyRepository;
    private final WebAuthnManager webAuthnManager;
    private final List<PublicKeyCredentialParameters> supportedAlgorithms;

    @Autowired
    public AuthenticationService(UserService userService, JwtUtils jwtUtils, @Value("${webauthn.rp.id}") String rpId, @Value("${webauthn.origin.url}") String originUrl, ChallengeRepository challengeRepository, PasskeyRepository passkeyRepository) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.rpId = rpId;
        this.challengeRepository = challengeRepository;
        this.origin = new Origin(originUrl);
        this.passkeyRepository = passkeyRepository;
        this.webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
        this.supportedAlgorithms = List.of(
                new PublicKeyCredentialParameters(PUBLIC_KEY, EdDSA),
                new PublicKeyCredentialParameters(PUBLIC_KEY, ES256),
                new PublicKeyCredentialParameters(PUBLIC_KEY, RS256)
        );
    }

    public AuthenticationResponse registerUser(RegistrationData data, String userAgent, String sessionId) throws ChallengeExpiredException, NoChallengeIssuedException, UserNotFoundException {
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
        UserDto user = userService.registerPasskey(challenge.getCreatedUser(), Passkey.From(userAgent, verifiedData));
        challengeRepository.delete(challenge);
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
        ChallengeEntity challenge = challengeRepository.findById(sessionId).orElseThrow(NoChallengeIssuedException::new);
        if (!challenge.isExpired()) {
            return challenge;
        }
        if (challenge.getCreatedUser() != null) {
            userService.deleteUser(challenge.getCreatedUser());
        }
        challengeRepository.delete(challenge);
        throw new ChallengeExpiredException("Challenge expired at %s".formatted(challenge.getExpires()));
    }

    public ChallengeDto generateChallenge() {
        return ChallengeDto.From(challengeRepository.save(ChallengeEntity.randomChallenge()));
    }

    public PublicKeyCredentialCreationOptionsDto startRegistration(UserCreationParametersDto params) throws IncorrectEmailFormatException, EmailAlreadyRegisteredException {
        UserDto user = userService.createUser(params);
        ChallengeEntity challenge = ChallengeEntity.randomChallenge(user);
        challengeRepository.save(challenge);
        PublicKeyCredentialCreationOptions opts = new PublicKeyCredentialCreationOptionsBuilder(
                new PublicKeyCredentialRpEntity(rpId),
                new PublicKeyCredentialUserEntity(convertUUIDToByteArray(user.id()), user.email(), user.displayName()),
                new DefaultChallenge(challenge.getValue()),
                supportedAlgorithms)
                .hints(List.of(CLIENT_DEVICE, HYBRID, SECURITY_KEY))
                .authenticatorSelection(new AuthenticatorSelectionCriteria(PLATFORM, true, ResidentKeyRequirement.REQUIRED, UserVerificationRequirement.DISCOURAGED))
                .build();
        return PublicKeyCredentialCreationOptionsDto.from(opts, challenge);
    }

    private static byte[] convertUUIDToByteArray(UUID uuid) {
        // Source - https://stackoverflow.com/a/2983319
        // Posted by aioobe
        // Retrieved 2026-02-16, License - CC BY-SA 2.5
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return Base64.getUrlEncoder().withoutPadding().encode(bb.array());

    }

    public void cancelSession(String sessionId) {
        Optional<ChallengeEntity> challengeOpt = challengeRepository.findById(sessionId);
        if (challengeOpt.isEmpty()) return;
        ChallengeEntity challenge = challengeOpt.get();
        if (challenge.getCreatedUser() != null) {
            userService.deleteUser(challenge.getCreatedUser());
        }
        challengeRepository.delete(challenge);
    }
}
