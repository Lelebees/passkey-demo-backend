package nl.lelebees.passkeydemo.backend.security.application.exception;

public class PasskeyNotFoundException extends Exception {
    public PasskeyNotFoundException(String message) {
        super(message);
    }

    public PasskeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PasskeyNotFoundException(Throwable cause) {
        super(cause);
    }

    public PasskeyNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PasskeyNotFoundException() {
    }
}
