package nl.lelebees.passkeydemo.backend.security.application.exception;

public class NoChallengeIssuedException extends Exception {
    public NoChallengeIssuedException() {
        super();
    }

    public NoChallengeIssuedException(String message) {
        super(message);
    }

    public NoChallengeIssuedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoChallengeIssuedException(Throwable cause) {
        super(cause);
    }

    public NoChallengeIssuedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
