package nl.lelebees.passkeydemo.backend.application;

import com.webauthn4j.data.*;
import nl.lelebees.passkeydemo.backend.application.dto.AuthenticationResponse;
import nl.lelebees.passkeydemo.backend.application.dto.UserAuthenticationParametersDto;
import nl.lelebees.passkeydemo.backend.application.dto.UserCreationParametersDto;
import nl.lelebees.passkeydemo.backend.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.application.exception.ChallengeExpiredException;
import nl.lelebees.passkeydemo.backend.application.exception.EmailAlreadyRegisteredException;
import nl.lelebees.passkeydemo.backend.application.exception.PasskeyNotFoundException;
import nl.lelebees.passkeydemo.backend.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.domain.ChallengeEntity;
import nl.lelebees.passkeydemo.backend.domain.Email;
import nl.lelebees.passkeydemo.backend.domain.IncorrectEmailFormatException;
import nl.lelebees.passkeydemo.backend.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

import static com.webauthn4j.data.UserVerificationRequirement.PREFERRED;

@Service
public class AuthenticationService {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final String rpId;

    @Autowired
    public AuthenticationService(UserService userService, JwtUtils jwtUtils, @Value("${webauthn.rp.id}") String rpId) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.rpId = rpId;
    }

    public PublicKeyCredentialCreationOptions startRegisterProcess(UserCreationParametersDto optionsDto) throws IncorrectEmailFormatException, EmailAlreadyRegisteredException {
        var rp = new PublicKeyCredentialRpEntity(rpId);
        ChallengeEntity challenge = ChallengeEntity.randomChallenge();
        UserDto user = userService.createUser(optionsDto, challenge);
        return new PublicKeyCredentialCreationOptions(rp, new PublicKeyCredentialUserEntity(convertUUIDToByteArray(user.id()), user.email(), user.displayName()), challenge, new ArrayList<>());
    }

    public AuthenticationResponse registerUser(RegistrationData data, Email email, String userAgent) throws UserNotFoundException, ChallengeExpiredException {
        UserDto user = userService.registerPasskey(data, email, userAgent);
        return new AuthenticationResponse(jwtUtils.generateJwtToken(user.email()));
    }

    public PublicKeyCredentialRequestOptions startAuthenticationProcess(UserAuthenticationParametersDto authDto) throws IncorrectEmailFormatException, UserNotFoundException {
        ChallengeEntity challenge = userService.challenge(new Email(authDto.email()));
        return new PublicKeyCredentialRequestOptions(challenge, 300000L, rpId, null, PREFERRED, null);
    }

    public AuthenticationResponse authenticateUser(AuthenticationData data) throws PasskeyNotFoundException, ChallengeExpiredException {
        UserDto userDto = userService.authenticatePasskey(data);
        return new AuthenticationResponse(jwtUtils.generateJwtToken(userDto.email()));
    }

    private static byte[] convertUUIDToByteArray(UUID uuid) {
        // Source - https://stackoverflow.com/a/2983319
        // Posted by aioobe
        // Retrieved 2026-02-16, License - CC BY-SA 2.5
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();

    }
}
