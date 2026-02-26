package nl.lelebees.passkeydemo.backend.security.application.exception;

public class ChallengeExpiredException extends Exception {
    public ChallengeExpiredException(String message) {
        super(message);
    }
}
