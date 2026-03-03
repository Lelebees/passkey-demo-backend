package nl.lelebees.passkeydemo.backend.security.application;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.*;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.verifier.exception.UserNotVerifiedException;
import com.webauthn4j.verifier.exception.VerificationException;
import nl.lelebees.passkeydemo.backend.security.application.dto.AuthenticationRequestOptionsDto;
import nl.lelebees.passkeydemo.backend.security.application.dto.ChallengeDto;
import nl.lelebees.passkeydemo.backend.security.application.dto.PublicKeyCredentialCreationOptionsDto;
import nl.lelebees.passkeydemo.backend.security.application.dto.UserCreationParametersDto;
import nl.lelebees.passkeydemo.backend.security.application.dto.jwt.AuthRefreshResponseDto;
import nl.lelebees.passkeydemo.backend.security.application.dto.jwt.AuthenticationResponseDto;
import nl.lelebees.passkeydemo.backend.security.application.exception.*;
import nl.lelebees.passkeydemo.backend.security.application.jwt.JwtToken;
import nl.lelebees.passkeydemo.backend.security.application.jwt.JwtUtils;
import nl.lelebees.passkeydemo.backend.security.data.ChallengeRepository;
import nl.lelebees.passkeydemo.backend.security.domain.ChallengeEntity;
import nl.lelebees.passkeydemo.backend.security.domain.PublicKeyCredentialCreationOptionsBuilder;
import nl.lelebees.passkeydemo.backend.user.application.UserService;
import nl.lelebees.passkeydemo.backend.user.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.user.application.dto.UserOverviewDto;
import nl.lelebees.passkeydemo.backend.user.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.user.data.PasskeyRepository;
import nl.lelebees.passkeydemo.backend.user.domain.Email;
import nl.lelebees.passkeydemo.backend.user.domain.IncorrectEmailFormatException;
import nl.lelebees.passkeydemo.backend.user.domain.Passkey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.*;

import static com.webauthn4j.data.AuthenticatorAttachment.CROSS_PLATFORM;
import static com.webauthn4j.data.PublicKeyCredentialHints.*;
import static com.webauthn4j.data.PublicKeyCredentialType.PUBLIC_KEY;
import static com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.*;

@Service
public class AuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
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
                new PublicKeyCredentialParameters(PUBLIC_KEY, ES256),
                new PublicKeyCredentialParameters(PUBLIC_KEY, EdDSA),
                new PublicKeyCredentialParameters(PUBLIC_KEY, RS256),
                new PublicKeyCredentialParameters(PUBLIC_KEY, RS512),
                new PublicKeyCredentialParameters(PUBLIC_KEY, RS384)
        );
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

    public AuthenticationResponseDto registerUser(RegistrationData data, String userAgent, String sessionId) throws ChallengeExpiredException, NoChallengeIssuedException, UserNotFoundException, IncorrectEmailFormatException {
        ChallengeEntity challenge = findBySession(sessionId);
        // If verification fails, user must retry with new challenge
        challengeRepository.delete(challenge);
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
            logger.info("Verification failed.", e);
            throw new UserNotVerifiedException("Could not register user", e);
        }
        UserDto user = userService.registerPasskey(challenge.getCreatedUser(), userAgent, verifiedData);
        return AuthenticationResponseDto.from(jwtUtils, user.email());
    }

    public AuthenticationResponseDto authenticateUser(AuthenticationData data, String sessionId) throws PasskeyNotFoundException, ChallengeExpiredException, NoChallengeIssuedException, UserNotFoundException, IncorrectEmailFormatException {
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
        return AuthenticationResponseDto.from(jwtUtils, passkey.getOwnerEmail().toString());
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
        UserOverviewDto user = userService.createUser(params);
        ChallengeEntity challenge = ChallengeEntity.randomChallenge(user);
        challengeRepository.save(challenge);
        PublicKeyCredentialCreationOptions opts = new PublicKeyCredentialCreationOptionsBuilder(
                new PublicKeyCredentialRpEntity(rpId),
                new PublicKeyCredentialUserEntity(convertUUIDToByteArray(user.id()), user.email(), user.displayName()),
                new DefaultChallenge(challenge.getValue()),
                supportedAlgorithms)
                .hints(List.of(CLIENT_DEVICE, HYBRID, SECURITY_KEY))
                .authenticatorSelection(new AuthenticatorSelectionCriteria(CROSS_PLATFORM, false, ResidentKeyRequirement.PREFERRED, UserVerificationRequirement.DISCOURAGED))
                .attestation(AttestationConveyancePreference.NONE)
                .timeout(challenge.getTimeoutDuration().toMillis())
                .build();
        return PublicKeyCredentialCreationOptionsDto.from(opts, challenge);
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

    public AuthenticationRequestOptionsDto startAuthentication() {
        ChallengeEntity challenge = challengeRepository.save(ChallengeEntity.randomChallenge());
        return new AuthenticationRequestOptionsDto(new PublicKeyCredentialRequestOptions(new DefaultChallenge(
                challenge.getValue()),
                challenge.getTimeoutDuration().toMillis(),
                rpId,
                Collections.emptyList(),
                UserVerificationRequirement.PREFERRED,
                List.of(CLIENT_DEVICE, HYBRID, SECURITY_KEY),
                null), challenge.getSessionId());
    }

    public AuthRefreshResponseDto refreshAccessToken(JwtToken refreshToken) throws InvalidTokenException, IncorrectEmailFormatException, UserNotFoundException {
        if (!jwtUtils.isValidRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Refresh token is invalid or expired");
        }
        if (userService.isRefreshTokenRetracted(new Email(jwtUtils.extractUsername(refreshToken)), refreshToken))
        {
            throw new InvalidTokenException("Refresh token has been retracted");
        }
        return new AuthRefreshResponseDto(jwtUtils.generateAccessToken(refreshToken));
    }

    public void signOut(UUID id) throws UserNotFoundException {
        userService.revokeRefreshTokens(id);
    }
}
