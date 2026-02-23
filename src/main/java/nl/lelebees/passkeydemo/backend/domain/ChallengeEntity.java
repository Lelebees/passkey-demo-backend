package nl.lelebees.passkeydemo.backend.domain;

import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.util.Base64UrlUtil;
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
    @Column(name = "session_id")
    private String sessionId;
    @Column(name = "value")
    private byte[] challengeBytes;

    @Column(name = "issued_at")
    private Instant issued;

    @Column(name = "expires_at")
    private Instant expires;

    public ChallengeEntity(byte[] challenge, String sessionId) {
        this.challengeBytes = challenge;
        this.issued = Instant.now();
        this.expires = issued.plus(5, MINUTES);
        this.sessionId = sessionId;
    }

    protected ChallengeEntity() {

    }

    public static ChallengeEntity randomChallenge() {
        Random r = new Random();
        byte[] challenge = new byte[32];
        r.nextBytes(challenge);
        byte[] sessionIdBytes = new byte[16];
        r.nextBytes(sessionIdBytes);
        String sessionId = Base64UrlUtil.encodeToString(sessionIdBytes);
        return new ChallengeEntity(challenge, sessionId);
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

    public String getSessionId() {
        return sessionId;
    }
}
