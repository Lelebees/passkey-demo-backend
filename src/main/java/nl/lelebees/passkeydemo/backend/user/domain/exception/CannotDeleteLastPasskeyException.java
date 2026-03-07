package nl.lelebees.passkeydemo.backend.user.domain.exception;

public class CannotDeleteLastPasskeyException extends Exception {
    public CannotDeleteLastPasskeyException() {
    }

    public CannotDeleteLastPasskeyException(String message) {
        super(message);
    }

    public CannotDeleteLastPasskeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public CannotDeleteLastPasskeyException(Throwable cause) {
        super(cause);
    }

    public CannotDeleteLastPasskeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
