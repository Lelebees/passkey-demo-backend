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
import nl.lelebees.passkeydemo.backend.application.dto.UserCreationParametersDto;
import nl.lelebees.passkeydemo.backend.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.application.exception.ChallengeExpiredException;
import nl.lelebees.passkeydemo.backend.application.exception.EmailAlreadyRegisteredException;
import nl.lelebees.passkeydemo.backend.application.exception.PasskeyNotFoundException;
import nl.lelebees.passkeydemo.backend.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.data.PasskeyRepository;
import nl.lelebees.passkeydemo.backend.data.UserRepository;
import nl.lelebees.passkeydemo.backend.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasskeyRepository passkeyRepository;
    private final Origin origin;
    private final String rpId;
    private final WebAuthnManager webAuthnManager;

    @Autowired
    public UserService(UserRepository repository, PasskeyRepository passkeyRepository, @Value("${webauthn.origin.url}") String originUrl, @Value("${webauthn.rp.id}") String rpId) {
        this.repository = repository;
        this.passkeyRepository = passkeyRepository;
        this.origin = new Origin(originUrl);
        this.rpId = rpId;
        this.webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
    }

    public UserService(UserRepository repository, PasskeyRepository passkeyRepository, Origin origin, String rpId, WebAuthnManager webAuthnManager) {
        this.repository = repository;
        this.passkeyRepository = passkeyRepository;
        this.origin = origin;
        this.rpId = rpId;
        this.webAuthnManager = webAuthnManager;
    }

    public UserDto getUserById(UUID id) throws UserNotFoundException {
        return UserDto.From(getFromOptional(repository.findById(id)));
    }

    public UserDto createUser(UserCreationParametersDto optionsDto, ChallengeEntity challenge) throws EmailAlreadyRegisteredException, IncorrectEmailFormatException {
        Email email = new Email(optionsDto.email());
        if (repository.existsUserByEmail(email)) {
            throw new EmailAlreadyRegisteredException("%s is already registered.");
        }
        return UserDto.From(repository.save(new User(email, optionsDto.displayName(), challenge)));
    }

    public UserDto getUserByEmail(Email email) throws UserNotFoundException {
        return UserDto.From(getFromOptional(repository.findUserByEmail(email)));
    }

    private User getFromOptional(Optional<User> opt) throws UserNotFoundException {
        return opt.orElseThrow(UserNotFoundException::new);
    }

    public UserDto registerPasskey(RegistrationData data, Email email, String userAgent) throws UserNotFoundException, ChallengeExpiredException {
        User user = getFromOptional(repository.findUserByEmail(email));
        if (user.getIssuedChallenge().isExpired()) {
            if (user.getPasskeys().isEmpty()) {
                repository.delete(user);
            }
            throw new ChallengeExpiredException("Challenge expired before registry could complete.");
        }
        ServerProperty server = ServerProperty.builder()
                .origin(origin)
                .rpId(rpId)
                .challenge(user.getIssuedChallenge())
                .build();
        RegistrationParameters parameters = new RegistrationParameters(server, null, false, true);
        RegistrationData verifiedData;
        try {
            verifiedData = webAuthnManager.verify(data, parameters);
        } catch (VerificationException e) {
            throw new UserNotVerifiedException("Could not verify user", e);
        }
        user.registerKey(Passkey.From(userAgent, verifiedData));
        return UserDto.From(repository.save(user));
    }

    public ChallengeEntity challenge(Email email) throws UserNotFoundException {
        User user = getFromOptional(repository.findUserByEmail(email));
        ChallengeEntity challenge = user.issueNewChallenge();
        repository.save(user);
        return challenge;
    }

    public ChallengeEntity getChallengeFor(byte[] credentialId) throws PasskeyNotFoundException, ChallengeExpiredException {
        Passkey passkey = passkeyRepository.findById(credentialId).orElseThrow(PasskeyNotFoundException::new);
        ChallengeEntity issuedChallenge = passkey.getOwner().getIssuedChallenge();
        if (issuedChallenge.isExpired()) {
            throw new ChallengeExpiredException("Challenge expired before authentication could complete.");
        }
        return issuedChallenge;
    }

    public UserDto authenticatePasskey(AuthenticationData data) throws ChallengeExpiredException, PasskeyNotFoundException, VerificationException {
        Passkey passkey = passkeyRepository.findById(data.getCredentialId()).orElseThrow(PasskeyNotFoundException::new);
        ChallengeEntity issuedChallenge = passkey.getOwner().getIssuedChallenge();
        if (issuedChallenge.isExpired()) {
            throw new ChallengeExpiredException("Challenge expired before authentication could complete.");
        }
        ServerProperty serverProperty = ServerProperty.builder()
                .origin(origin)
                .rpId(rpId)
                .challenge(issuedChallenge)
                .build();
        //expectations
        boolean userVerificationRequired = true;
        boolean userPresenceRequired = true;
        AuthenticationParameters authenticationParameters = new AuthenticationParameters(
                serverProperty,
                passkey.getCredentialRecord(),
                null,
                userVerificationRequired,
                userPresenceRequired
        );
        passkey.getCredentialRecord().setCounter(webAuthnManager.verify(data, authenticationParameters).getAuthenticatorData().getSignCount());
        return UserDto.From(passkeyRepository.save(passkey).getOwner());
    }
}
