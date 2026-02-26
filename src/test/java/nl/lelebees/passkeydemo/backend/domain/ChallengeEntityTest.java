package nl.lelebees.passkeydemo.backend.domain;

import nl.lelebees.passkeydemo.backend.security.domain.ChallengeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChallengeEntityTest {

    @Test
    @DisplayName("Challenge expires only after the set expiry time.")
    void isExpired() {
        ChallengeEntity e = ChallengeEntity.randomChallenge();
        assertFalse(e.isExpired());
    }

    @Test
    @DisplayName("Challenge expires after set expiry time.")
    void expires() {
        ChallengeEntity e = new ChallengeEntity("", new byte[] {}, Instant.now().minus(5, MINUTES), Instant.now().minus(5, SECONDS), UUID.randomUUID());
        assertTrue(e.isExpired());
    }
}