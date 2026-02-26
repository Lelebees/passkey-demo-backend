package nl.lelebees.passkeydemo.backend.security.application.dto;

import com.webauthn4j.data.client.challenge.Challenge;
import jakarta.annotation.Nonnull;
import nl.lelebees.passkeydemo.backend.security.domain.ChallengeEntity;

import java.time.Instant;

public record ChallengeDto(byte[] value, Instant expires_at, String session_id) implements Challenge {

    public static ChallengeDto From(ChallengeEntity entity) {
        return new ChallengeDto(entity.getValue(), entity.getExpires(), entity.getSessionId());
    }

    @Nonnull
    @Override
    public byte[] getValue() {
        return value;
    }
}
