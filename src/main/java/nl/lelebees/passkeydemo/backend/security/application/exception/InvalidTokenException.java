package nl.lelebees.passkeydemo.backend.security.application.exception;

public class InvalidTokenException extends Exception {
    public InvalidTokenException(String message) {
        super(message);
    }
}
