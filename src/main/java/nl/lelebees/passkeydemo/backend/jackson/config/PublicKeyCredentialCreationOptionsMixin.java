package nl.lelebees.passkeydemo.backend.jackson.config;

import com.webauthn4j.converter.jackson.serializer.json.ChallengeSerializer;
import com.webauthn4j.data.client.challenge.Challenge;
import tools.jackson.databind.annotation.JsonSerialize;

public abstract class PublicKeyCredentialCreationOptionsMixin {
    @JsonSerialize(using = ChallengeSerializer.class)
    public abstract Challenge getChallenge();
}
