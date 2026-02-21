package nl.lelebees.passkeydemo.backend.domain;

import com.webauthn4j.data.client.challenge.Challenge;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.Formula;

import java.time.Instant;
import java.util.Arrays;
import java.util.Random;

@Entity(name = "challenge")
public class ChallengeEntity implements Challenge {

    @Id
    @Column(name = "value")
    private byte[] challengeBytes;

    @Column(name = "issued_at")
    private Instant issued;

    @Formula("issued_at + (300000 * interval '1 ms')")
    private Instant expires;

    public ChallengeEntity(byte[] challenge) {
        this.challengeBytes = challenge;
        this.issued = Instant.now();
    }

    protected ChallengeEntity() {

    }

    public static ChallengeEntity randomChallenge() {
        byte[] challenge = new byte[32];
        new Random().nextBytes(challenge);
        return new ChallengeEntity(challenge);
    }

    @Override
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
