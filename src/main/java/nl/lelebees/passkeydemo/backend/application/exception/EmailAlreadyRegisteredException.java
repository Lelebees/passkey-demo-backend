package nl.lelebees.passkeydemo.backend.application.exception;

public class EmailAlreadyRegisteredException extends Exception {
    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }
}
