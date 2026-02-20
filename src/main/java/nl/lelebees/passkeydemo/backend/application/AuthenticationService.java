package nl.lelebees.passkeydemo.backend.application;

import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import com.webauthn4j.data.PublicKeyCredentialRpEntity;
import com.webauthn4j.data.PublicKeyCredentialUserEntity;
import com.webauthn4j.data.RegistrationData;
import nl.lelebees.passkeydemo.backend.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.application.dto.UserCreationParametersDto;
import nl.lelebees.passkeydemo.backend.application.exception.EmailAlreadyRegisteredException;
import nl.lelebees.passkeydemo.backend.domain.Email;
import nl.lelebees.passkeydemo.backend.domain.IncorrectEmailFormatException;
import nl.lelebees.passkeydemo.backend.domain.ChallengeEntity;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final UserService userService;

    public AuthenticationService(UserService userService) {
        this.userService = userService;
    }


    public PublicKeyCredentialCreationOptions startRegisterProcess(UserCreationParametersDto optionsDto) throws IncorrectEmailFormatException, EmailAlreadyRegisteredException {
        var rp = new PublicKeyCredentialRpEntity("me?");
        ChallengeEntity challenge = ChallengeEntity.randomChallenge();
        UserDto user = userService.createUser(optionsDto, challenge);
        return new PublicKeyCredentialCreationOptions(rp, new PublicKeyCredentialUserEntity(convertUUIDToByteArray(user.id()), user.email(), user.displayName()), challenge, new ArrayList<>());
    }

    public void registerUser(RegistrationData data, Email email) {
        userService.getUserByEmail(email);
        data.getCollectedClientData().getChallenge();
        data.getCollectedClientData();
        CredentialRecord r = new CredentialRecordImpl();
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
