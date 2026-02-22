package nl.lelebees.passkeydemo.backend.domain;

import com.webauthn4j.data.client.challenge.Challenge;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.Arrays;
import java.util.Random;

import static java.time.temporal.ChronoUnit.MINUTES;

@Entity(name = "challenge")
public class ChallengeEntity implements Challenge {

    @Id
    @Column(name = "value")
    private byte[] challengeBytes;

    @Column(name = "issued_at")
    private Instant issued;

    @Column(name = "expires_at")
    private Instant expires;

    public ChallengeEntity(byte[] challenge) {
        this.challengeBytes = challenge;
        this.issued = Instant.now();
        this.expires = issued.plus(5, MINUTES);
    }

    protected ChallengeEntity() {

    }

    public static ChallengeEntity randomChallenge() {
        byte[] challenge = new byte[32];
        new Random().nextBytes(challenge);
        return new ChallengeEntity(challenge);
    }

    @Override
    @Nonnull
    public byte[] getValue() {
        return Arrays.copyOf(challengeBytes, challengeBytes.length);
    }

    public Instant getIssued() {
        return issued;
    }

    public Instant getExpires() {
        return expires;
    }

    public boolean isExpired() {
        return Instant.now().isBefore(expires);
    }
}
