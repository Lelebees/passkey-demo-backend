package nl.lelebees.passkeydemo.backend.application.exception;

public class ChallengeExpiredException extends Exception {
    public ChallengeExpiredException(String message) {
        super(message);
    }
}
