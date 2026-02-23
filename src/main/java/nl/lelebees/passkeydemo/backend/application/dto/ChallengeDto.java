package nl.lelebees.passkeydemo.backend.application.dto;

import nl.lelebees.passkeydemo.backend.domain.ChallengeEntity;

import java.time.Instant;

public record ChallengeDto(byte[] challenge, Instant expires_at, String sessionId) {

    public static ChallengeDto From(ChallengeEntity entity) {
        return new ChallengeDto(entity.getValue(), entity.getExpires(), entity.getSessionId());
    }
}
