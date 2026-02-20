package nl.lelebees.passkeydemo.backend.domain;

import com.webauthn4j.data.client.challenge.Challenge;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Random;

@Entity(name = "challenge")
public class ChallengeEntity implements Challenge {

    @Id
    @Column(name = "value")
    private byte[] challengeBytes;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User challengedUser;

    private Instant issued;


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

    public User getChallengedUser() {
        return challengedUser;
    }

    @Override
    public byte[] getValue() {
        return challengeBytes;
    }

}
