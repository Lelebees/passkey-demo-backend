package nl.lelebees.passkeydemo.backend.security.application.dto;

import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import nl.lelebees.passkeydemo.backend.security.domain.ChallengeEntity;

public record PublicKeyCredentialCreationOptionsDto(PublicKeyCredentialCreationOptions options, String sessionId) {

    public static PublicKeyCredentialCreationOptionsDto from(PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions, ChallengeEntity challenge) {
        return new PublicKeyCredentialCreationOptionsDto(publicKeyCredentialCreationOptions, challenge.getSessionId());
    }
}
