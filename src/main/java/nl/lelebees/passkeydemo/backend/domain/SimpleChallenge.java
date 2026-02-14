package nl.lelebees.passkeydemo.backend.domain;

import com.webauthn4j.data.client.challenge.Challenge;

import java.util.Random;

public class SimpleChallenge implements Challenge {

    byte[] challengeBytes;

    public SimpleChallenge(byte[] challenge) {
        this.challengeBytes = challenge;
    }

    @Override
    public byte[] getValue() {
        return challengeBytes;
    }

    public static SimpleChallenge random() {
        byte[] challenge = new byte[32];
        new Random().nextBytes(challenge);
        return new SimpleChallenge(challenge);
    }

}
