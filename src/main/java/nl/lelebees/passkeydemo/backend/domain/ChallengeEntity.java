package nl.lelebees.passkeydemo.backend.domain;

import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.util.Base64UrlUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import nl.lelebees.passkeydemo.backend.application.dto.UserDto;

import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

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

    @Nullable
    @Column(name = "created_user_id")
    private UUID createdUser;

    public ChallengeEntity(byte[] challenge, String sessionId, @Nullable UUID createdUser) {
        this.challengeBytes = challenge;
        this.createdUser = createdUser;
        this.issued = Instant.now();
        this.expires = issued.plus(5, MINUTES);
        this.sessionId = sessionId;
    }

    ChallengeEntity(String sessionId, byte[] challengeBytes, Instant issued, Instant expires, @Nullable UUID createdUser) {
        this.sessionId = sessionId;
        this.challengeBytes = challengeBytes;
        this.issued = issued;
        this.expires = expires;
        this.createdUser = createdUser;
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
        return new ChallengeEntity(challenge, sessionId, null);
    }

    public static ChallengeEntity randomChallenge(UserDto user) {
        Random r = new Random();
        byte[] challenge = new byte[32];
        r.nextBytes(challenge);
        byte[] sessionIdBytes = new byte[16];
        r.nextBytes(sessionIdBytes);
        String sessionId = Base64UrlUtil.encodeToString(sessionIdBytes);
        return new ChallengeEntity(challenge, sessionId, user.id());
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
        return Instant.now().isAfter(expires);
    }

    public String getSessionId() {
        return sessionId;
    }

    @Nullable
    public UUID getCreatedUser() {
        return createdUser;
    }
}
