package nl.lelebees.passkeydemo.backend.user.domain;

public class IncorrectEmailFormatException extends Exception {
    public IncorrectEmailFormatException(String s) {
        super(s);
    }
}
